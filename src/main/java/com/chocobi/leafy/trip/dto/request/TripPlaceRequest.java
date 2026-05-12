package com.chocobi.leafy.trip.dto.request;

import lombok.Data;


@Data
public class TripPlaceRequest {
    private Long placeId;
    private int visitOrder;
    private int dayIndex;
    private String memo;
}
