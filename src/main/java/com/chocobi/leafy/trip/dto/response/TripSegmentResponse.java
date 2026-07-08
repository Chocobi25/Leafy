package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.vo.TripTransport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripSegmentResponse {
    @Schema(description = "여행 구간 ID")
    private Long id;

    @Schema(description = "여행 ID")
    private Long tripId;

    @Schema(description = "출발 장소 ID")
    private Long startPlaceId;

    @Schema(description = "도착 장소 ID")
    private Long endPlaceId;

    @Schema(description = "이동수단")
    private TripTransport transport;

    @Schema(description = "이동 거리(m)")
    private double distance;

    @Schema(description = "이동 시간(분)")
    private int duration;

    @Schema(description = "탄소 배출량")
    private double carbonEmission;

    public static TripSegmentResponse from(TripSegmentEntity tripSegment) {
        return TripSegmentResponse.builder()
                .id(tripSegment.getId())
                .tripId(tripSegment.getRouteOption().getTrip().getId())
                .startPlaceId(tripSegment.getStartTripPlace().getPlace().getId())
                .endPlaceId(tripSegment.getEndTripPlace().getPlace().getId())
                .transport(tripSegment.getRouteOption().getTransport())
                .distance(tripSegment.getDistance())
                .duration(tripSegment.getDuration())
                .carbonEmission(tripSegment.getCarbonEmission())
                .build();
    }
}
