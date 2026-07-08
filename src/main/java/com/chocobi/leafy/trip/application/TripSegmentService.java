package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.dto.CarDistanceResponse;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.service.TransDistanceService;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.response.TripSegmentResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    public DistanceResponse calculateAndSaveOwnedCarRoute(Long tripId, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(tripId, userId);
        validateTripEditable(trip);
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        return calculateAndSaveCarRoute(tripId, tripPlaces);
    }

    public List<RouteCalculationResult> calculateAndSaveOwnedPublicRoute(Long tripId, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(tripId, userId);
        validateTripEditable(trip);
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        return calculateAndSavePublicRoute(tripId, tripPlaces);
    }

    @Transactional(readOnly = true)
    public List<TripSegmentResponse> getTripSegments(TripEntity trip) {
        return tripSegmentFindService.findConfirmedTripSegments(trip.getId()).stream()
                .map(TripSegmentResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTripSegments(TripEntity trip) {
        tripSegmentCommandService.deleteAll(trip);
        tripRouteOptionCommandService.deleteAll(trip);
    }

    private DistanceResponse calculateAndSaveCarRoute(Long tripId, List<TripPlaceResponse> tripPlaces) {
        validateTripPlacesForRouteCalculation(tripPlaces);
        CarDistanceResponse carResponse = carDistanceService.calculateTripDistance(tripId, tripPlaces);
        List<Section> sections = carResponse.getSections();
        tripRouteCandidateService.saveRouteCandidate(tripId, sections, TripTransport.CAR, tripPlaces);

        return carResponse.getDistanceResponse();
    }

    private List<RouteCalculationResult> calculateAndSavePublicRoute(Long tripId, List<TripPlaceResponse> tripPlaces) {
        validateTripPlacesForRouteCalculation(tripPlaces);
        List<RouteCalculationResult> results = transDistanceService.calculateTripDistance(tripId, tripPlaces);
        List<Section> sections = results.stream()
                .map(this::toSection)
                .toList();

        tripRouteCandidateService.saveRouteCandidate(tripId, sections, TripTransport.PUBLIC, tripPlaces);

        return results;
    }

    private void validateTripPlacesForRouteCalculation(List<TripPlaceResponse> tripPlaces) {
        if (tripPlaces == null || tripPlaces.size() < 2) {
            throw new CustomException(TripError.INVALID_TRIP_ROUTE_CANDIDATE);
        }
    }

    private void validateTripEditable(TripEntity trip) {
        if (!trip.isEditable()) {
            throw new CustomException(TripError.TRIP_NOT_EDITABLE);
        }
    }

    private Section toSection(RouteCalculationResult result) {
        return Section.builder()
                .distance((int) result.getTotalDistance())
                .duration(result.getTotalTime())
                .carbonEmission(result.getCarbonEmission())
                .maxCarbonEmission(result.getMaxCarbonEmission())
                .build();
    }

}
