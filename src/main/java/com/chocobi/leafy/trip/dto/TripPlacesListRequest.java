package com.chocobi.leafy.trip.dto;

import lombok.Data;

import java.util.List;

@Data
public class TripPlacesListRequest {
    private Long TripId;
    private List<TripPlaceRequest> places;
}
