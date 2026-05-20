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

    @PositiveOrZero
    private int visitOrder;

    @PositiveOrZero
    private int dayIndex;

    private String memo;
}
