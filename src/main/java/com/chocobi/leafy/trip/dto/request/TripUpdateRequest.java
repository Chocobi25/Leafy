package com.chocobi.leafy.trip.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TripUpdateRequest(
        @Schema(description = "여행 제목")
        @NotBlank @Size(max = 50)
        String title,

        @Schema(description = "여행 시작 날짜")
        @NotNull
        LocalDate startDate,

        @Schema(description = "여행 종료 날짜")
        @NotNull
        LocalDate endDate
) {
        @AssertTrue(message = "여행 종료 날짜는 시작 날짜보다 빠를 수 없습니다.")
        public boolean isValidDateRange() {
                return startDate == null || endDate == null || !startDate.isAfter(endDate);
        }
}
