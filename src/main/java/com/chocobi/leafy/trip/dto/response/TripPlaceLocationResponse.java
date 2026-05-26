package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripPlaceLocationResponse {
    private Long id;
    private String title;
    private String address;
    private double latitude;
    private double longitude;

    public static TripPlaceLocationResponse from(PlaceEntity place) {
        return TripPlaceLocationResponse.builder()
                .id(place.getId())
                .title(place.getTitle())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .build();
    }
}
