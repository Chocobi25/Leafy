package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripRouteOptionFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void completeRouteCandidate(Long tripId, String transport) {
        if (transport == null) throw new IllegalArgumentException("transport가 필요합니다.");

        TripEntity trip = tripFindService.findTrip(tripId);
        TripRouteOptionEntity selectedRouteOption = tripRouteOptionFindService.findTripRouteOption(tripId, transport);
        List<TripRouteOptionEntity> routeOptions = tripRouteOptionFindService.findTripRouteOptions(tripId);

        tripRouteOptionCommandService.confirmOnly(selectedRouteOption, routeOptions);
        trip.clearRouteStale();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTotalTimeAndCarbon(Long tripId, String transport) {
        TripRouteOptionEntity routeOption = tripRouteOptionFindService.findTripRouteOption(tripId, transport);

        Map<String, Object> result = new HashMap<>();
        result.put("totalDuration", routeOption.getTotalDuration());
        result.put("totalCarbonEmission", routeOption.getTotalCarbonEmission());

        return result;
    }

    private void validateRouteCandidate(List<Section> sections, List<TripPlaceResponse> tripPlaces) {
        if (tripPlaces == null || tripPlaces.size() < 2) {
            throw new IllegalArgumentException("여행 장소는 2개 이상 필요합니다.");
        }

        if (sections == null || sections.isEmpty()) {
            throw new IllegalArgumentException("저장할 여행 경로 구간이 없습니다.");
        }
    }

    private void validateTripPlacesCoverAllTripDays(TripEntity trip, List<TripPlaceResponse> tripPlaces) {
        long totalDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        Set<Integer> tripPlaceDays = tripPlaces.stream()
                .map(TripPlaceResponse::getDayIndex)
                .collect(Collectors.toSet());

        for (int dayIndex = 0; dayIndex < totalDays; dayIndex++) {
            if (!tripPlaceDays.contains(dayIndex)) {
                throw new IllegalArgumentException("여행 기간의 모든 일차에 여행 장소가 필요합니다.");
            }
        }
    }

    private void deleteRouteCandidate(Long tripId, TripTransport transport) {
        tripRouteOptionFindService.findTripRouteOptionCandidate(tripId, transport)
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
