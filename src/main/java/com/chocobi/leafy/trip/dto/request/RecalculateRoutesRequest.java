package com.chocobi.leafy.trip.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RecalculateRoutesRequest {
    private Long tripId;
    private String transport;
    private List<TripPlaceRequest> places;
}