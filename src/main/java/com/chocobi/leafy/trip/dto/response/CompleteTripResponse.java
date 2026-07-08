package com.chocobi.leafy.trip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteTripResponse {
    @Schema(description = "완료된 여행 ID")
    private Long tripId;

    public static CompleteTripResponse from(Long tripId) {
        return CompleteTripResponse.builder()
                .tripId(tripId)
                .build();
    }
}
