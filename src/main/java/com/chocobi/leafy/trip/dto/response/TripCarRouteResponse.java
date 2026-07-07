package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.distance.domain.DistanceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripCarRouteResponse {
    @Schema(description = "총 이동 거리(m)")
    private double distance;

    @Schema(description = "총 이동 시간(초)")
    private int duration;

    @Schema(description = "탄소 배출량")
    private double carbonEmission;

    public static TripCarRouteResponse from(DistanceResponse response) {
        return TripCarRouteResponse.builder()
                .distance(response.getDistance())
                .duration(response.getDuration())
                .carbonEmission(response.getCarbonEmission())
                .build();
    }
}
