package com.chocobi.leafy.trip.dto.request;

import com.chocobi.leafy.trip.vo.TripTransport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CompleteTripRequest(
        @Schema(description = "확정할 이동수단. 요청 본문에서는 car 또는 public을 사용합니다.", allowableValues = {"car", "public"})
        @NotNull
        TripTransport transport
) {
}
