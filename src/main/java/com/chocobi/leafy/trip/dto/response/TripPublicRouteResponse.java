package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripPublicRouteResponse {
    @Schema(description = "경로 타입")
    private int pathType;

    @Schema(description = "총 이동 시간(초)")
    private int totalTime;

    @Schema(description = "총 이동 거리(m)")
    private double totalDistance;

    @Schema(description = "탄소 배출량")
    private double carbonEmission;

    @Schema(description = "비교 기준 최대 탄소 배출량")
    private double maxCarbonEmission;

    @Schema(description = "버스 이동 거리(m)")
    private int busDistance;

    @Schema(description = "지하철 이동 거리(m)")
    private int subwayDistance;

    @Schema(description = "기차 이동 거리(m)")
    private int trainDistance;

    @Schema(description = "항공 이동 거리(m)")
    private int airplaneDistance;

    public static TripPublicRouteResponse from(RouteCalculationResult result) {
        return TripPublicRouteResponse.builder()
                .pathType(result.getPathType())
                .totalTime(result.getTotalTime())
                .totalDistance(result.getTotalDistance())
                .carbonEmission(result.getCarbonEmission())
                .maxCarbonEmission(result.getMaxCarbonEmission())
                .busDistance(result.getBusDistance())
                .subwayDistance(result.getSubwayDistance())
                .trainDistance(result.getTrainDistance())
                .airplaneDistance(result.getAirplaneDistance())
                .build();
    }
}
