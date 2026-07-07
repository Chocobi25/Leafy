package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripRouteOptionFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripRouteCandidateService {
    private final TripFindService tripFindService;
    private final TripRouteOptionCommandService tripRouteOptionCommandService;
    private final TripRouteOptionFindService tripRouteOptionFindService;
    private final TripPlaceService tripPlaceService;

    @Transactional
    public void saveRouteCandidate(Long tripId, List<Section> sections, TripTransport transport, List<TripPlaceResponse> tripPlaces) {
        validateRouteCandidate(sections, tripPlaces);

        TripEntity trip = tripFindService.findTrip(tripId);
        validateTripPlacesCoverAllTripDays(trip, tripPlaces);
        deleteRouteCandidate(tripId, transport);

        TripRouteOptionEntity routeOption = createRouteOption(
                trip,
                transport,
                sections
        );
        routeOption.replaceSegments(createTripSegments(sections, tripPlaces));
        tripRouteOptionCommandService.save(routeOption);
    }

    @Transactional
    public void completeRouteCandidateForTrip(Long tripId, String transport) {
        TripTransport tripTransport = TripTransport.from(transport);
        TripEntity trip = tripFindService.findTrip(tripId);
        confirmRouteCandidate(trip, tripTransport);
    }

    @Transactional
    public void completeOwnedRouteCandidate(Long tripId, String transport, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(tripId, userId);
        confirmRouteCandidate(trip, TripTransport.from(transport));
        trip.editStatus(TripStatus.READY);
    }

    private void confirmRouteCandidate(TripEntity trip, TripTransport transport) {
        TripRouteOptionEntity selectedRouteOption = tripRouteOptionFindService.findOptionalRouteCandidate(trip.getId(), transport)
                .orElseThrow(() -> new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));
        List<TripRouteOptionEntity> routeOptions = tripRouteOptionFindService.findTripRouteOptions(trip.getId());

        tripRouteOptionCommandService.confirmOnly(selectedRouteOption, routeOptions);
        trip.clearRouteStale();
    }

    @Transactional(readOnly = true)
    public TripRouteCandidateSummaryResponse getOwnedRouteSummary(Long tripId, String transport, Long userId) {
        tripFindService.findOwnedTrip(tripId, userId);
        return getRouteSummary(tripId, transport);
    }

    private TripRouteCandidateSummaryResponse getRouteSummary(Long tripId, String transport) {
        TripRouteOptionEntity routeOption = tripRouteOptionFindService.findRouteCandidate(tripId, transport);
        return TripRouteCandidateSummaryResponse.from(routeOption);
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

    private TripRouteOptionEntity createRouteOption(
            TripEntity trip,
            TripTransport transport,
            List<Section> sections
    ) {
        return TripRouteOptionEntity.builder()
                .trip(trip)
                .transport(transport)
                .totalDistance(sections.stream().mapToDouble(Section::getDistance).sum())
                .totalDuration(sections.stream().mapToInt(this::toDurationInMinutes).sum())
                .totalCarbonEmission(sections.stream().mapToDouble(Section::getCarbonEmission).sum())
                .confirmed(false)
                .build();
    }

    private List<TripSegmentEntity> createTripSegments(
            List<Section> sections,
            List<TripPlaceResponse> tripPlaces
    ) {
        List<TripPlaceResponse> sortedTripPlaces = new ArrayList<>(tripPlaces);
        sortedTripPlaces.sort(tripPlaceRouteOrder());

        List<TripSegmentEntity> tripSegments = new ArrayList<>();
        for (int i = 0; i < sortedTripPlaces.size() - 1 && i < sections.size(); i++) {
            tripSegments.add(createTripSegment(sections.get(i), sortedTripPlaces.get(i), sortedTripPlaces.get(i + 1)));
        }

        return tripSegments;
    }

    private TripSegmentEntity createTripSegment(
            Section section,
            TripPlaceResponse startPlace,
            TripPlaceResponse endPlace
    ) {
        TripPlaceEntity startTripPlace = tripPlaceService.getTripPlaceById(startPlace.getTripPlaceId());
        TripPlaceEntity endTripPlace = tripPlaceService.getTripPlaceById(endPlace.getTripPlaceId());

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
