package com.chocobi.leafy.trip.dto.request;

import com.chocobi.leafy.trip.vo.TripTransport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TripRouteSummaryRequest(
        @Schema(description = "이동수단. 쿼리 파라미터에서는 enum 이름인 CAR 또는 PUBLIC을 사용합니다.", allowableValues = {"CAR", "PUBLIC"})
        @NotNull
        TripTransport transport
) {
}
