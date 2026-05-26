package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.dto.request.CreateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.request.UpdateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlacesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "여행 계획 API", description = "여행 계획에 필요한 장소 목록 저장 및 수정")
public interface TripPlanDocs {

    @Operation(summary = "여행 장소 목록 초기 생성")
    ResponseEntity<SuccessResponse<TripPlacesResponse>> createTripPlaces(
            @PathVariable @Positive Long tripId,
            @NotEmpty(message = "여행에는 여행 장소가 하나 이상 존재해야 합니다.")
            @RequestBody List<@Valid @NotNull CreateTripPlaceRequest> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    );

    @Operation(summary = "여행 장소 목록 수정")
    ResponseEntity<SuccessResponse<TripPlacesResponse>> updateTripPlaces(
            @PathVariable @Positive Long tripId,
            @NotEmpty(message = "여행에는 여행 장소가 하나 이상 존재해야 합니다.")
            @RequestBody List<@Valid @NotNull UpdateTripPlaceRequest> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    );
}
