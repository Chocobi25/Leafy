package com.chocobi.leafy.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

public record TripRequest(
        @Schema(name = "여행 제목")
        @NotBlank @Size(max = 50)
        String title,

        @Schema(name = "여행 시작 날짜")
        @NotNull
        LocalDate startDate,

        @Schema(name = "여행 종료 날짜")
        @NotNull
        LocalDate endDate,

        @Schema(name = "출발지")
        @NotBlank
        String departure,

        @Schema(name = "목적지")
        @NotBlank
        String arrival
) {
    @AssertTrue(message = "여행 종료 날짜는 시작 날짜보다 빠를 수 없습니다.")
    public boolean isValidDateRange() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}
