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
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.chocobi.leafy.distance.service.DistanceUtils.placeToPoint;

@Service
@RequiredArgsConstructor
public class TripSegmentService {
    private final TripSegmentFindService tripSegmentFindService;
    private final TripSegmentCommandService tripSegmentCommandService;
    private final TripRouteOptionCommandService tripRouteOptionCommandService;
    private final TripRouteCandidateService tripRouteCandidateService;
    private final CarDistanceService carDistanceService;
    private final TransDistanceService transDistanceService;
    private final TripPlaceService tripPlaceService;
    private final PlaceService placeService;

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
        tripRouteCandidateService.saveRouteCandidate(tripId, sections, TripTransport.CAR, tripPlaces);

        return carResponse.getDistanceResponse();
    }

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

        tripRouteCandidateService.saveRouteCandidate(batchRequest.getTripId(), sections, TripTransport.PUBLIC, tripPlaces);

        return results;
    }

    @Transactional(readOnly = true)
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

}
