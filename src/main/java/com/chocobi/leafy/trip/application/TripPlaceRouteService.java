package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.trip.dto.request.RecalculateRoutesRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPlaceRouteService {
    private final TripFindService tripFindService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;
    private final TripRouteCandidateService tripRouteCandidateService;

    public void editTripPlacesAndRecalculateRoutes(RecalculateRoutesRequest request, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(request.getTripId(), userId);

        tripPlaceService.updateTripPlaces(request.getTripId(), request.getPlaces(), userId);
        List<TripPlaceResponse> updatedTripPlaces = tripPlaceService.getTripPlaces(trip.getId());
        tripSegmentService.recalculateRoutesAndSave(trip, request.getTransport(), updatedTripPlaces);
        tripRouteCandidateService.completeRouteCandidateForTrip(trip.getId(), request.getTransport());
    }
}
