package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripPlaceFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripRouteOptionFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripRouteCandidateService {
    private final TripFindService tripFindService;
    private final TripRouteOptionCommandService tripRouteOptionCommandService;
    private final TripRouteOptionFindService tripRouteOptionFindService;
    private final TripPlaceFindService tripPlaceFindService;

    @Transactional
    public void saveRouteCandidate(Long tripId, List<Section> sections, TripTransport transport, List<TripPlaceResponse> tripPlaces) {
        validateRouteCandidate(sections, tripPlaces);

        TripEntity trip = tripFindService.findTrip(tripId);
        validateTripPlacesCoverAllTripDays(trip, tripPlaces);
        deleteRouteCandidate(tripId, transport);

        TripRouteOptionEntity routeOption = TripRouteOptionEntity.builder()
                .trip(trip)
                .transport(transport)
                .totalDistance(sections.stream().mapToDouble(Section::getDistance).sum())
                .totalDuration(sections.stream().mapToInt(this::toDurationInMinutes).sum())
                .totalCarbonEmission(sections.stream().mapToDouble(Section::getCarbonEmission).sum())
                .confirmed(false)
                .build();
        routeOption.replaceSegments(createTripSegments(sections, tripPlaces));
        tripRouteOptionCommandService.save(routeOption);
        trip.clearRouteStale();
    }

    @Transactional
    public void completeOwnedRouteCandidate(Long tripId, TripTransport transport, Long userId) {
        validateTransport(transport);
        TripEntity trip = tripFindService.findOwnedTrip(tripId, userId);
        validateTripEditable(trip);
        confirmRouteCandidate(trip, transport);
    }

    @Transactional(readOnly = true)
    public TripRouteCandidateSummaryResponse getOwnedRouteSummary(Long tripId, TripTransport transport, Long userId) {
        tripFindService.findOwnedTrip(tripId, userId);
        return getRouteSummary(tripId, transport);
    }

    private void confirmRouteCandidate(TripEntity trip, TripTransport transport) {
        validateCurrentRouteCandidate(trip);
        TripRouteOptionEntity selectedRouteOption = tripRouteOptionFindService.findOptionalRouteCandidate(trip.getId(), transport)
                .orElseThrow(() -> new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));
        List<TripRouteOptionEntity> routeOptions = tripRouteOptionFindService.findTripRouteOptions(trip.getId());

        tripRouteOptionCommandService.confirmOnly(selectedRouteOption, routeOptions);
        trip.completeRoute();
    }

    private void validateCurrentRouteCandidate(TripEntity trip) {
        if (trip.isRouteStale()) {
            throw new CustomException(TripError.STALE_TRIP_ROUTE_CANDIDATE);
        }
    }

    private void validateTripEditable(TripEntity trip) {
        if (!trip.isEditable()) {
            throw new CustomException(TripError.TRIP_NOT_EDITABLE);
        }
    }

    private TripRouteCandidateSummaryResponse getRouteSummary(Long tripId, TripTransport transport) {
        TripRouteOptionEntity routeOption = tripRouteOptionFindService.findRouteCandidate(tripId, transport);
        return TripRouteCandidateSummaryResponse.from(routeOption);
    }

    private void validateTransport(TripTransport transport) {
        if (transport == null) {
            throw new CustomException(TripError.INVALID_TRIP_TRANSPORT);
        }
    }

    private void validateRouteCandidate(List<Section> sections, List<TripPlaceResponse> tripPlaces) {
        if (tripPlaces == null || tripPlaces.size() < 2) {
            throw new CustomException(TripError.INVALID_TRIP_ROUTE_CANDIDATE);
        }

        if (sections == null || sections.isEmpty()) {
            throw new CustomException(TripError.INVALID_TRIP_ROUTE_CANDIDATE);
        }

        if (sections.size() != tripPlaces.size() - 1) {
            throw new CustomException(TripError.INVALID_TRIP_ROUTE_CANDIDATE);
        }
    }

    private void validateTripPlacesCoverAllTripDays(TripEntity trip, List<TripPlaceResponse> tripPlaces) {
        long totalDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        boolean hasInvalidDayIndex = tripPlaces.stream()
                .map(TripPlaceResponse::getDayIndex)
                .anyMatch(dayIndex -> dayIndex < 0 || dayIndex >= totalDays);

        if (hasInvalidDayIndex) {
            throw new CustomException(TripError.INVALID_TRIP_ROUTE_CANDIDATE);
        }

        Set<Integer> tripPlaceDays = tripPlaces.stream()
                .map(TripPlaceResponse::getDayIndex)
                .collect(Collectors.toSet());

        for (int dayIndex = 0; dayIndex < totalDays; dayIndex++) {
            if (!tripPlaceDays.contains(dayIndex)) {
                throw new CustomException(TripError.INVALID_TRIP_ROUTE_CANDIDATE);
            }
        }
    }

    private void deleteRouteCandidate(Long tripId, TripTransport transport) {
        tripRouteOptionFindService.findOptionalRouteCandidate(tripId, transport)
                .ifPresent(tripRouteOptionCommandService::delete);
    }

    private List<TripSegmentEntity> createTripSegments(
            List<Section> sections,
            List<TripPlaceResponse> tripPlaces
    ) {
        List<TripPlaceResponse> sortedTripPlaces = new ArrayList<>(tripPlaces);
        sortedTripPlaces.sort(tripPlaceRouteOrder());
        Map<Long, TripPlaceEntity> tripPlaceMap = createTripPlaceMap(sortedTripPlaces);

        List<TripSegmentEntity> tripSegments = new ArrayList<>();
        for (int i = 0; i < sortedTripPlaces.size() - 1 && i < sections.size(); i++) {
            tripSegments.add(createTripSegment(
                    sections.get(i),
                    sortedTripPlaces.get(i),
                    sortedTripPlaces.get(i + 1),
                    tripPlaceMap
            ));
        }

        return tripSegments;
    }

    private Map<Long, TripPlaceEntity> createTripPlaceMap(List<TripPlaceResponse> tripPlaces) {
        List<Long> tripPlaceIds = tripPlaces.stream()
                .map(TripPlaceResponse::getTripPlaceId)
                .toList();

        return tripPlaceFindService.findTripPlaces(tripPlaceIds).stream()
                .collect(Collectors.toMap(TripPlaceEntity::getId, Function.identity()));
    }

    private TripSegmentEntity createTripSegment(
            Section section,
            TripPlaceResponse startPlace,
            TripPlaceResponse endPlace,
            Map<Long, TripPlaceEntity> tripPlaceMap
    ) {
        TripPlaceEntity startTripPlace = tripPlaceMap.get(startPlace.getTripPlaceId());
        TripPlaceEntity endTripPlace = tripPlaceMap.get(endPlace.getTripPlaceId());

        return TripSegmentEntity.builder()
                .startTripPlace(startTripPlace)
                .endTripPlace(endTripPlace)
                .distance(section.getDistance())
                .duration(toDurationInMinutes(section))
                .carbonEmission(section.getCarbonEmission())
                .build();
    }

    private Comparator<TripPlaceResponse> tripPlaceRouteOrder() {
        return Comparator.comparing(TripPlaceResponse::getDayIndex)
                .thenComparing(TripPlaceResponse::getVisitOrder);
    }

    private int toDurationInMinutes(Section section) {
        return Math.max(1, section.getDuration() / 60);
    }
}
