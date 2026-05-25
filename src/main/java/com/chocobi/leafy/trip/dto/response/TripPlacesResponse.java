package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TripPlacesResponse {
    private Long tripId;
    private boolean routeStale;
    private List<TripPlaceResponse> tripPlaces;

    public static TripPlacesResponse from(TripEntity trip, List<TripPlaceResponse> tripPlaces) {
        return TripPlacesResponse.builder()
                .tripId(trip.getId())
                .routeStale(trip.isRouteStale())
                .tripPlaces(tripPlaces)
                .build();
    }
}
