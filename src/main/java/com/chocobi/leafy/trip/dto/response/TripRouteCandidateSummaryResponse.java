package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripRouteCandidateSummaryResponse {

    @Schema(description = "총 이동 시간(분)")
    private int totalDuration;

    @Schema(description = "총 탄소 배출량")
    private double totalCarbonEmission;

    public static TripRouteCandidateSummaryResponse from(TripRouteOptionEntity routeOption) {
        return TripRouteCandidateSummaryResponse.builder()
                .totalDuration(routeOption.getTotalDuration())
                .totalCarbonEmission(routeOption.getTotalCarbonEmission())
                .build();
    }
}
