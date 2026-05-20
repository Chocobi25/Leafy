package com.chocobi.leafy.trip.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TripPlaceRequest {
    @NotNull
    @Positive
    private Long placeId;

    @NotNull
    @PositiveOrZero
    private Integer visitOrder;

    @NotNull
    @PositiveOrZero
    private Integer dayIndex;

    private String memo;
}
