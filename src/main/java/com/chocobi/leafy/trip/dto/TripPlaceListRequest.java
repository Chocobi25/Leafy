package com.chocobi.leafy.trip.dto;

import lombok.Data;

import java.util.List;

@Data
public class TripPlaceListRequest {
    private Long TripId;
    private List<TripPlaceRequest> placeList;
}
