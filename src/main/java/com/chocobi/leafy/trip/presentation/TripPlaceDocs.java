package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "여행 장소 API", description = "여행에 속한 장소 목록 저장 및 수정")
public interface TripPlaceDocs {

    @Operation(summary = "여행 장소 목록 저장")
    ResponseEntity<SuccessResponse<List<TripPlaceResponse>>> saveTripPlaces(
            @PathVariable @Positive Long tripId,
            @Valid @NotEmpty @RequestBody List<TripPlaceRequest> request,
            Authentication authentication
    );
}
