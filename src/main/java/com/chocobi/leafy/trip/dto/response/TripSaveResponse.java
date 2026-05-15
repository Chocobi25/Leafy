package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TripSaveResponse {

    @Schema(description = "여행 ID")
    private Long tripId;

    @Schema(description = "여행 상태")
    private TripStatus status;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static TripSaveResponse from(TripEntity trip) {
        return TripSaveResponse.builder()
                .tripId(trip.getId())
                .status(trip.getStatus())
                .createdAt(trip.getCreatedAt())
                .build();
    }
}
