package com.chocobi.leafy.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateTripPlaceRequest(
        @Schema(description = "장소 ID")
        @NotNull
        @Positive
        Long placeId,

        @Schema(description = "방문 순서")
        @NotNull
        @PositiveOrZero
        Integer visitOrder,

        @Schema(description = "여행 일차")
        @NotNull
        @PositiveOrZero
        Integer dayIndex,

        @Schema(description = "장소 메모")
        @Size(max = 255)
        String memo
) {
}
