package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.application.TripRouteService;
import com.chocobi.leafy.trip.dto.request.CompleteTripRequest;
import com.chocobi.leafy.trip.dto.request.TripRouteSummaryRequest;
import com.chocobi.leafy.trip.dto.response.CompleteTripResponse;
import com.chocobi.leafy.trip.dto.response.TripCarRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripPublicRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripRouteController implements TripRouteDocs {

    private final TripRouteService tripRouteService;

    @PostMapping("/{tripId}/complete")
    public ResponseEntity<SuccessResponse<CompleteTripResponse>> completeTrip(
            @PathVariable @Positive Long tripId,
            @Valid @RequestBody CompleteTripRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                tripRouteService.completeTrip(tripId, request, userId)
        ));
    }

    @GetMapping("/{tripId}/summary")
    public ResponseEntity<SuccessResponse<TripRouteCandidateSummaryResponse>> getRouteSummary(
            @PathVariable @Positive Long tripId,
            @Valid @ModelAttribute TripRouteSummaryRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                tripRouteService.getRouteSummary(tripId, request, userId)
        ));
    }

    @PostMapping("/{tripId}/routes/car")
    public ResponseEntity<SuccessResponse<TripCarRouteResponse>> calculateCarRoute(
            @PathVariable @Positive Long tripId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                tripRouteService.calculateCarRoute(tripId, userId)
        ));
    }

    @PostMapping("/{tripId}/routes/public")
    public ResponseEntity<SuccessResponse<List<TripPublicRouteResponse>>> calculatePublicRoute(
            @PathVariable @Positive Long tripId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                tripRouteService.calculatePublicRoute(tripId, userId)
        ));
    }
}
