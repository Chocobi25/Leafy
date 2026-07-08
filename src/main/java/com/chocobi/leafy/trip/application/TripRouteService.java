package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.trip.dto.request.CompleteTripRequest;
import com.chocobi.leafy.trip.dto.request.TripRouteSummaryRequest;
import com.chocobi.leafy.trip.dto.response.CompleteTripResponse;
import com.chocobi.leafy.trip.dto.response.TripCarRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripPublicRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripRouteService {
    private final TripRouteCandidateService tripRouteCandidateService;
    private final TripSegmentService tripSegmentService;
    private final TripMessageService tripMessageService;

    public CompleteTripResponse completeTrip(Long tripId, CompleteTripRequest request, Long userId) {
        tripRouteCandidateService.completeOwnedRouteCandidate(tripId, request.transport(), userId);
        tripMessageService.notifyTripCreated(userId, tripId);
        return CompleteTripResponse.from(tripId);
    }

    public TripRouteCandidateSummaryResponse getRouteSummary(Long tripId, TripRouteSummaryRequest request, Long userId) {
        return tripRouteCandidateService.getOwnedRouteSummary(tripId, request.transport(), userId);
    }

    public TripCarRouteResponse calculateCarRoute(Long tripId, Long userId) {
        return TripCarRouteResponse.from(tripSegmentService.calculateAndSaveOwnedCarRoute(tripId, userId));
    }

    public List<TripPublicRouteResponse> calculatePublicRoute(Long tripId, Long userId) {
        return tripSegmentService.calculateAndSaveOwnedPublicRoute(tripId, userId).stream()
                .map(TripPublicRouteResponse::from)
                .toList();
    }
}
