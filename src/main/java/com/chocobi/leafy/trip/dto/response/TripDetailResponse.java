package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.dto.TripSegmentDTO;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TripDetailResponse {

    @Schema(description = "여행 ID")
    private Long tripId;

    @Schema(description = "여행 제목")
    private String title;

    @Schema(description = "여행 시작일")
    private LocalDate startDate;

    @Schema(description = "여행 종료일")
    private LocalDate endDate;

    @Schema(description = "출발 지역")
    private String departure;

    @Schema(description = "도착 지역")
    private String arrival;

    @Schema(description = "절감 탄소량")
    private double carbonSaved;

    @Schema(description = "배출 탄소량")
    private double carbonEmission;

    @Schema(description = "여행 상태")
    private TripStatus status;

    @Schema(description = "인증 일시")
    private LocalDateTime certificationAt;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;

    @Schema(description = "여행 구간 목록")
    private List<TripSegmentDTO> tripSegments;

    public static TripDetailResponse from(TripEntity trip, List<TripSegmentDTO> tripSegments) {
        return TripDetailResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .departure(trip.getDeparture().getName())
                .arrival(trip.getArrival().getName())
                .carbonSaved(trip.getCarbonSaved())
                .carbonEmission(trip.getCarbonEmission())
                .status(trip.getStatus())
                .certificationAt(trip.getCertificationAt())
                .userId(trip.getUser().getId())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .tripSegments(tripSegments)
                .build();
    }
}
