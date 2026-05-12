package com.chocobi.leafy.trip.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TripPlacesListRequest {
    private Long tripId;
    private List<TripPlaceRequest> places;
    String transport;
}
