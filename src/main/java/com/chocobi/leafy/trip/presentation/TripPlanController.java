package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.application.TripPlaceService;
import com.chocobi.leafy.trip.dto.request.CreateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.request.UpdateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlacesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripPlanController implements TripPlanDocs {

    private final TripPlaceService tripPlaceService;

    // TODO: 여행 계획 흐름을 정리할 때 route, segment, summary, complete API를 이 컨트롤러로 옮긴다.
    @PostMapping("/{tripId}/places")
    public ResponseEntity<SuccessResponse<TripPlacesResponse>> createTripPlaces(
            @PathVariable @Positive Long tripId,
            @NotEmpty(message = "여행에는 여행 장소가 하나 이상 존재해야 합니다.")
            @RequestBody List<@Valid @NotNull CreateTripPlaceRequest> request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                tripPlaceService.createTripPlaces(tripId, request, userId)
        ));
    }

    @PutMapping("/{tripId}/places")
    public ResponseEntity<SuccessResponse<TripPlacesResponse>> updateTripPlaces(
            @PathVariable @Positive Long tripId,
            @NotEmpty(message = "여행에는 여행 장소가 하나 이상 존재해야 합니다.")
            @RequestBody List<@Valid @NotNull UpdateTripPlaceRequest> request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                tripPlaceService.updateTripPlaces(tripId, request, userId)
        ));
    }

}
