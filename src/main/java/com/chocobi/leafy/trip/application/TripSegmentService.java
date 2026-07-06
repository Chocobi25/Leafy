package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.domain.*;
import com.chocobi.leafy.distance.dto.CarDistanceResponse;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.service.DistanceUtils;
import com.chocobi.leafy.distance.service.TransDistanceService;
import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.trip.dto.request.UpdateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceLocationResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripSegmentDTO;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripRouteOptionFindService;
import com.chocobi.leafy.trip.infra.TripSegmentCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chocobi.leafy.distance.service.DistanceUtils.placeToPoint;

@Service
@RequiredArgsConstructor
public class TripSegmentService {
    private final TripSegmentFindService tripSegmentFindService;
    private final TripSegmentCommandService tripSegmentCommandService;
    private final TripRouteOptionCommandService tripRouteOptionCommandService;
    private final TripRouteOptionFindService tripRouteOptionFindService;
    private final TripFindService tripFindService;
    private final CarDistanceService carDistanceService;
    private final TransDistanceService transDistanceService;
    private final TripPlaceService tripPlaceService;
    private final PlaceService placeService;

    @Transactional
    public void completeTripSegments(Long tripId, String transport) {
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

    /**
     * DB에 TripSegment 저장
     */
    public void saveTripSegments(List<TripSegmentEntity> tripSegments) {
        if (tripSegments == null || tripSegments.isEmpty()) return;
        tripSegmentCommandService.saveAll(tripSegments);
    }

    private void saveRouteCandidate(Long tripId, List<Section> sections, String transport, List<TripPlaceResponse> tripPlaces) {
        validateRouteCandidate(sections, tripPlaces);

        TripEntity trip = tripFindService.findTrip(tripId);
        TripTransport tripTransport = TripTransport.from(transport);
        deleteRouteCandidate(tripId, tripTransport);

        TripRouteOptionEntity routeOption = tripRouteOptionCommandService.save(createRouteOptionFromSections(
                trip,
                tripTransport,
                sections,
                false
        ));
        saveTripSegments(createTripSegments(routeOption, sections, tripPlaces));
    }

    private void validateRouteCandidate(List<Section> sections, List<TripPlaceResponse> tripPlaces) {
        if (tripPlaces == null || tripPlaces.size() < 2) {
            throw new IllegalArgumentException("여행 장소는 2개 이상 필요합니다.");
        }

        if (sections == null || sections.isEmpty()) {
            throw new IllegalArgumentException("저장할 여행 경로 구간이 없습니다.");
        }
    }

    private void deleteRouteCandidate(Long tripId, TripTransport transport) {
        tripRouteOptionFindService.findTripRouteOptionCandidate(tripId, transport)
                .ifPresent(routeOption -> {
                    tripSegmentCommandService.deleteAllByRouteOption(routeOption);
                    tripRouteOptionCommandService.delete(routeOption);
                });
    }

    private List<TripSegmentEntity> createTripSegments(
            TripRouteOptionEntity routeOption,
            List<Section> sections,
            List<TripPlaceResponse> tripPlaces
    ) {
        List<TripPlaceResponse> sortedTripPlaces = new ArrayList<>(tripPlaces);
        sortedTripPlaces.sort(tripPlaceRouteOrder());

        List<TripSegmentEntity> tripSegments = new ArrayList<>();
        for (int i = 0; i < sortedTripPlaces.size() - 1 && i < sections.size(); i++) {
            tripSegments.add(createTripSegment(routeOption, sections.get(i), sortedTripPlaces.get(i), sortedTripPlaces.get(i + 1)));
        }

        return tripSegments;
    }

    private TripSegmentEntity createTripSegment(
            TripRouteOptionEntity routeOption,
            Section section,
            TripPlaceResponse startPlace,
            TripPlaceResponse endPlace
    ) {
        TripPlaceEntity startTripPlace = tripPlaceService.getTripPlaceById(startPlace.getTripPlaceId());
        TripPlaceEntity endTripPlace = tripPlaceService.getTripPlaceById(endPlace.getTripPlaceId());

        return TripSegmentEntity.builder()
                .routeOption(routeOption)
                .startTripPlace(startTripPlace)
                .endTripPlace(endTripPlace)
                .distance(section.getDistance())
                .duration(toDurationInMinutes(section))
                .carbonEmission(section.getCarbonEmission())
                .build();
    }

    @Transactional
    public DistanceResponse calculateAndSaveCarRoute(CarDistanceRequest request, Long tripId) {
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);

        CarDistanceResponse carResponse;

        if (DistanceUtils.isJejuTrip(tripPlaces)) {
            CarDistanceRequest modifiedRequest = carDistanceService.addPortsToRequest(request, tripPlaces);
            carResponse = carDistanceService.getDistance(modifiedRequest);
        } else {
            carResponse = carDistanceService.getDistance(request);
        }

        List<Section> sections = carResponse.getSections();
        saveRouteCandidate(tripId, sections, TripTransport.CAR.getCode(), tripPlaces);

        return carResponse.getDistanceResponse();
    }

    @Transactional
    public List<RouteCalculationResult> calculateAndSavePublicRoute(TransDistanceBatchRequest batchRequest, List<TripPlaceResponse> tripPlaces) {
        List<RouteCalculationResult> results = transDistanceService.getBatchDistance(batchRequest);

        List<Section> sections = new ArrayList<>();
        for (RouteCalculationResult result : results) {
            Section section = new Section();
            section.setDistance((int) result.getTotalDistance());
            section.setDuration(result.getTotalTime());
            section.setCarbonEmission(result.getCarbonEmission());
            section.setMaxCarbonEmission(result.getMaxCarbonEmission());
            sections.add(section);
        }

        saveRouteCandidate(batchRequest.getTripId(), sections, TripTransport.PUBLIC.getCode(), tripPlaces);

        return results;
    }

    @Transactional
    public List<TripSegmentDTO> getTripSegments(Long tripId) {
        return tripSegmentFindService.findConfirmedTripSegmentsByTripId(tripId).stream()
                .map(TripSegmentDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteTripSegments(TripEntity trip) {
        tripSegmentCommandService.deleteAllByTrip(trip);
        tripRouteOptionCommandService.deleteAllByTrip(trip);
    }

    /**
     * 재계산 진입점: 프론트에서 온 tripPlaceRequests 를 TripPlaceResponse로 변환한 뒤
     * 적절한 거리 서비스 메서드를 호출한다.
     */
    @Transactional
    public void recalculateRoutesAndSave(TripEntity trip, String transport, List<UpdateTripPlaceRequest> tripPlaceRequests) {
        List<TripPlaceResponse> tripPlaces = tripPlaceRequests.stream()
                .map(req -> TripPlaceResponse.builder()
                        .tripId(trip.getId())
                        .place(TripPlaceLocationResponse.from(placeService.getPlace(req.placeId())))
                        .dayIndex(req.dayIndex())
                        .visitOrder(req.visitOrder())
                        .memo(req.memo())
                        .build())
                .toList();

        System.out.println("[DEBUG] TripPlaces to recalc: " + tripPlaces);

        String normalized = transport == null ? "car" : transport.toLowerCase();
        if ("car".equals(normalized)) {
            CarDistanceRequest carRequest = new CarDistanceRequest();
            carRequest.setTripId(trip.getId());

            if (!tripPlaces.isEmpty()) {
                TripPlaceLocationResponse firstPlace = tripPlaces.get(0).getPlace();
                TripPlaceLocationResponse lastPlace = tripPlaces.get(tripPlaces.size() - 1).getPlace();

                carRequest.setOrigin(placeToPoint(firstPlace));
                carRequest.setDestination(placeToPoint(lastPlace));
            }

            if (tripPlaces.size() > 2) {
                carRequest.setWaypoints(
                        tripPlaces.subList(1, tripPlaces.size() - 1)
                                .stream()
                                .map(tp -> placeToPoint(tp.getPlace()))
                                .toList()
                );
            }

            System.out.println("[DEBUG] CarDistanceRequest: " + carRequest);
            calculateAndSaveCarRoute(carRequest, trip.getId());
        } else if ("public".equals(normalized)) {
            List<TransDistanceRequest> requests = new ArrayList<>();
            for (int i = 0; i < tripPlaces.size() - 1; i++) {
                TripPlaceLocationResponse start = tripPlaces.get(i).getPlace();
                TripPlaceLocationResponse end = tripPlaces.get(i + 1).getPlace();

                TransDistanceRequest req = new TransDistanceRequest();
                req.setStartX(String.valueOf(start.getLongitude()));
                req.setStartY(String.valueOf(start.getLatitude()));
                req.setEndX(String.valueOf(end.getLongitude()));
                req.setEndY(String.valueOf(end.getLatitude()));
              
                requests.add(req);
            }

            TransDistanceBatchRequest batchRequest = new TransDistanceBatchRequest();
            batchRequest.setTripId(trip.getId());
            batchRequest.setRequests(requests);

            System.out.println("[DEBUG] PublicTransport BatchRequest: " + batchRequest);
            calculateAndSavePublicRoute(batchRequest, tripPlaces);
        }
    }

    /**
     * 재계산 진입점: DB에서 조회한 TripPlaceResponse를 직접 사용
     * (프론트엔드에서 온 request가 아닌, DB에 저장된 최신 데이터 사용)
     */
    @Transactional
    public void recalculateRoutesAndSaveV2(TripEntity trip, String transport, List<TripPlaceResponse> tripPlaces) {
        System.out.println("[DEBUG] TripPlaces to recalc (from DB): " + tripPlaces);

        // visitOrder로 정렬
        List<TripPlaceResponse> sortedPlaces = new ArrayList<>(tripPlaces);
        sortedPlaces.sort(tripPlaceRouteOrder());

        String normalized = transport == null ? "car" : transport.toLowerCase();

        if ("car".equals(normalized)) {
            CarDistanceRequest carRequest = new CarDistanceRequest();
            carRequest.setTripId(trip.getId());

            if (!sortedPlaces.isEmpty()) {
                TripPlaceLocationResponse firstPlace = sortedPlaces.get(0).getPlace();
                TripPlaceLocationResponse lastPlace = sortedPlaces.get(sortedPlaces.size() - 1).getPlace();

                carRequest.setOrigin(placeToPoint(firstPlace));
                carRequest.setDestination(placeToPoint(lastPlace));

                if (sortedPlaces.size() > 2) {
                    carRequest.setWaypoints(
                            sortedPlaces.subList(1, sortedPlaces.size() - 1)
                                    .stream()
                                    .map(tp -> placeToPoint(tp.getPlace()))
                                    .toList()
                    );
                }
            }

            System.out.println("[DEBUG] CarDistanceRequest: " + carRequest);
            calculateAndSaveCarRoute(carRequest, trip.getId());

        } else if ("public".equals(normalized)) {
            List<TransDistanceRequest> requests = new ArrayList<>();

            for (int i = 0; i < sortedPlaces.size() - 1; i++) {
                TripPlaceLocationResponse start = sortedPlaces.get(i).getPlace();
                TripPlaceLocationResponse end = sortedPlaces.get(i + 1).getPlace();

                TransDistanceRequest req = new TransDistanceRequest();
                req.setStartX(String.valueOf(start.getLongitude()));
                req.setStartY(String.valueOf(start.getLatitude()));
                req.setEndX(String.valueOf(end.getLongitude()));
                req.setEndY(String.valueOf(end.getLatitude()));

                requests.add(req);
            }

            TransDistanceBatchRequest batchRequest = new TransDistanceBatchRequest();
            batchRequest.setTripId(trip.getId());
            batchRequest.setRequests(requests);

            System.out.println("[DEBUG] PublicTransport BatchRequest: " + batchRequest);
            calculateAndSavePublicRoute(batchRequest, sortedPlaces);
        }
    }

    private Comparator<TripPlaceResponse> tripPlaceRouteOrder() {
        return Comparator.comparing(TripPlaceResponse::getDayIndex)
                .thenComparing(TripPlaceResponse::getVisitOrder);
    }

    private TripRouteOptionEntity createRouteOptionFromSections(
            TripEntity trip,
            TripTransport transport,
            List<Section> sections,
            boolean confirmed
    ) {
        return TripRouteOptionEntity.builder()
                .trip(trip)
                .transport(transport)
                .totalDistance(sections.stream().mapToDouble(Section::getDistance).sum())
                .totalDuration(sections.stream().mapToInt(this::toDurationInMinutes).sum())
                .totalCarbonEmission(sections.stream().mapToDouble(Section::getCarbonEmission).sum())
                .confirmed(confirmed)
                .build();
    }

    private int toDurationInMinutes(Section section) {
        return Math.max(1, section.getDuration() / 60);
    }
}
