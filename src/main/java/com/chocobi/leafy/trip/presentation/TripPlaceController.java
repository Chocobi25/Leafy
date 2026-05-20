package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.application.TripPlaceService;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripPlaceController implements TripPlaceDocs {

    private final TripPlaceService tripPlaceService;

    @PutMapping("/{tripId}/places")
    public ResponseEntity<SuccessResponse<List<TripPlaceResponse>>> saveTripPlaces(
            @PathVariable @Positive Long tripId,
            @Valid @NotEmpty @RequestBody List<TripPlaceRequest> request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(SuccessResponse.of(
                tripPlaceService.saveTripPlaces(tripId, request, userId)
        ));
    }

}
