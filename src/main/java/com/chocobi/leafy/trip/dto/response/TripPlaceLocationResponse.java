package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripPlaceLocationResponse {
    @Schema(description = "장소 ID")
    private Long id;

    @Schema(description = "장소명")
    private String title;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "위도")
    private double latitude;

    @Schema(description = "경도")
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
