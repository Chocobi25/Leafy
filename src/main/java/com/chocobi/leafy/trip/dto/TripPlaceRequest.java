package com.chocobi.leafy.trip.dto;

import lombok.Data;


@Data
public class TripPlaceRequest {
    private Long placeId;
    private int visitOrder;
    private int dayIndex;
    private String memo;
}
