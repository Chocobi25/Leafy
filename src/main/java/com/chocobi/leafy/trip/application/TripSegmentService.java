package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.domain.*;
import com.chocobi.leafy.distance.dto.CarDistanceResponse;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.service.DistanceUtils;
import com.chocobi.leafy.distance.service.TransDistanceService;
import com.chocobi.leafy.trip.dto.response.TripPlaceLocationResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripSegmentDTO;
import com.chocobi.leafy.trip.infra.TripFindService;
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
    private final TripFindService tripFindService;
    private final CarDistanceService carDistanceService;
    private final TransDistanceService transDistanceService;
    private final TripPlaceService tripPlaceService;

    public DistanceResponse calculateAndSaveCarRoute(Long tripId) {
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        return calculateAndSaveCarRoute(tripId, tripPlaces);
    }

    private DistanceResponse calculateAndSaveCarRoute(Long tripId, List<TripPlaceResponse> tripPlaces) {
        CarDistanceRequest carRequest = createCarDistanceRequest(tripId, tripPlaces);

        CarDistanceResponse carResponse;

        if (DistanceUtils.isJejuTrip(tripPlaces)) {
            CarDistanceRequest modifiedRequest = carDistanceService.addPortsToRequest(carRequest, tripPlaces);
            carResponse = carDistanceService.getDistance(modifiedRequest);
        } else {
            carResponse = carDistanceService.getDistance(carRequest);
        }

        List<Section> sections = carResponse.getSections();
        tripRouteCandidateService.saveRouteCandidate(tripId, sections, TripTransport.CAR, tripPlaces);

        return carResponse.getDistanceResponse();
    }

    public DistanceResponse calculateAndSaveOwnedCarRoute(Long tripId, Long userId) {
        tripFindService.findOwnedTrip(tripId, userId);
        return calculateAndSaveCarRoute(tripId);
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

    public List<RouteCalculationResult> calculateAndSaveOwnedPublicRoute(TransDistanceBatchRequest batchRequest, Long userId) {
        tripFindService.findOwnedTrip(batchRequest.getTripId(), userId);
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(batchRequest.getTripId());
        TransDistanceBatchRequest serverBatchRequest = createTransDistanceBatchRequest(batchRequest.getTripId(), tripPlaces);
        return calculateAndSavePublicRoute(serverBatchRequest, tripPlaces);
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

    public void recalculateRoutesAndSave(TripEntity trip, String transport, List<TripPlaceResponse> tripPlaces) {
        System.out.println("[DEBUG] TripPlaces to recalc (from DB): " + tripPlaces);

        List<TripPlaceResponse> sortedPlaces = new ArrayList<>(tripPlaces);
        sortedPlaces.sort(tripPlaceRouteOrder());

        String normalized = transport == null ? "car" : transport.toLowerCase();

        if ("car".equals(normalized)) {
            CarDistanceRequest carRequest = createCarDistanceRequest(trip.getId(), sortedPlaces);
            System.out.println("[DEBUG] CarDistanceRequest: " + carRequest);
            calculateAndSaveCarRoute(trip.getId(), sortedPlaces);

        } else if ("public".equals(normalized)) {
            TransDistanceBatchRequest batchRequest = createTransDistanceBatchRequest(trip.getId(), sortedPlaces);
            System.out.println("[DEBUG] PublicTransport BatchRequest: " + batchRequest);
            calculateAndSavePublicRoute(batchRequest, sortedPlaces);
        }
    }

    private CarDistanceRequest createCarDistanceRequest(Long tripId, List<TripPlaceResponse> tripPlaces) {
        CarDistanceRequest carRequest = new CarDistanceRequest();
        carRequest.setTripId(tripId);

        if (!tripPlaces.isEmpty()) {
            TripPlaceLocationResponse firstPlace = tripPlaces.getFirst().getPlace();
            TripPlaceLocationResponse lastPlace = tripPlaces.getLast().getPlace();

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

        return carRequest;
    }

    private TransDistanceBatchRequest createTransDistanceBatchRequest(Long tripId, List<TripPlaceResponse> tripPlaces) {
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
        batchRequest.setTripId(tripId);
        batchRequest.setRequests(requests);
        return batchRequest;
    }

    private Comparator<TripPlaceResponse> tripPlaceRouteOrder() {
        return Comparator.comparing(TripPlaceResponse::getDayIndex)
                .thenComparing(TripPlaceResponse::getVisitOrder);
    }

}
