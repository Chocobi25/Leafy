package com.chocobi.leafy.trip.dto;

import lombok.Data;


@Data
public class TripPlaceRequest {
    private Long tripPlaceId;
    private int visitOrder;
    private int dayIndex;
    private String memo;
}
