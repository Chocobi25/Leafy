package com.chocobi.leafy.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePlaceRequest(
        @Schema(description = "장소명")
        @NotBlank String title,

        @Schema(description = "주소")
        @NotBlank String address,

        @Schema(description = "위도")
        double latitude,

        @Schema(description = "경도")
        double longitude,

        @Schema(description = "저작권")
        @NotBlank String copyright
) {
}
