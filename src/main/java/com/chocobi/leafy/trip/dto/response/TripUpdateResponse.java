package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TripUpdateResponse {

    @Schema(description = "여행 ID")
    private Long tripId;

    @Schema(description = "여행 제목")
    private String title;

    @Schema(description = "여행 시작일")
    private LocalDate startDate;

    @Schema(description = "여행 종료일")
    private LocalDate endDate;

    @Schema(description = "여행 상태")
    private TripStatus status;

    public static TripUpdateResponse from(TripEntity trip) {
        return TripUpdateResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .build();
    }
}
