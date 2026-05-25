package com.chocobi.leafy.trip.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateTripPlaceRequest(
        @Positive
        Long tripPlaceId,

        @NotNull
        @Positive
        Long placeId,

        @NotNull
        @PositiveOrZero
        Integer visitOrder,

        @NotNull
        @PositiveOrZero
        Integer dayIndex,

        @Size(max = 255)
        String memo
) {
}
