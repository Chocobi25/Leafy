package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.client.TransCoordDTO;
import com.chocobi.leafy.trip.dto.request.CreateTripRequest;
import com.chocobi.leafy.trip.dto.request.TripUpdateRequest;
import com.chocobi.leafy.trip.dto.response.TripDetailResponse;
import com.chocobi.leafy.trip.dto.response.TripListResponse;
import com.chocobi.leafy.trip.dto.response.TripSaveResponse;
import com.chocobi.leafy.trip.application.TripService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
public class TripController implements TripDocs {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<SuccessResponse<TripSaveResponse>> createTrip(
            @Valid @RequestBody CreateTripRequest createTripRequest,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(tripService.createTrip(createTripRequest, userId)));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<TripListResponse>>> getTrips(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(tripService.getTrips(userId)));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<SuccessResponse<TripDetailResponse>> getTripDetails(
            @PathVariable @Positive Long tripId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(tripService.getTripDetails(tripId, userId)));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable @Positive Long tripId,
            @AuthenticationPrincipal Long userId
    ) {
        tripService.deleteTrip(tripId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{tripId}")
    public ResponseEntity<SuccessResponse<TripDetailResponse>> updateTrip(
            @PathVariable @Positive Long tripId,
            @Valid @RequestBody TripUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(tripService.updateTripInfo(tripId, request, userId)));
    }

    @PostMapping("/{tripId}/certify")
    public ResponseEntity<String> certifyTrip(
            @PathVariable @Positive Long tripId,
            @Valid @RequestBody TransCoordDTO transCoordDTO,
            @AuthenticationPrincipal Long userId
    ) {
        tripService.certifyTrip(tripId, transCoordDTO, userId);

        return ResponseEntity.ok("여행이 성공적으로 인증되었습니다.");
    }
}
