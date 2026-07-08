package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class TripPlaceResponse {
    @Schema(description = "여행 장소 ID")
    private Long tripPlaceId;

    @Schema(description = "여행 ID")
    private Long tripId;

    @Schema(description = "장소 위치 정보")
    private TripPlaceLocationResponse place;

    @Schema(description = "방문 순서")
    private int visitOrder;

    @Schema(description = "여행 일차")
    private int dayIndex;

    @Schema(description = "장소 메모")
    private String memo;

    public static TripPlaceResponse from(TripPlaceEntity tripPlace) {
        return TripPlaceResponse.builder()
                .tripPlaceId(tripPlace.getId())
                .tripId(tripPlace.getTrip().getId())
                .place(TripPlaceLocationResponse.from(tripPlace.getPlace()))
                .visitOrder(tripPlace.getVisitOrder())
                .dayIndex(tripPlace.getDayIndex())
                .memo(tripPlace.getMemo())
                .build();
    }
}
