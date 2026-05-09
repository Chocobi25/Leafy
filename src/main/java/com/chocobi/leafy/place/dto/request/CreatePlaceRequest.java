package com.chocobi.leafy.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreatePlaceRequest(
        @Schema(description = "장소명")
        @NotBlank String title,

        @Schema(description = "주소")
        @NotBlank String address,

        @Schema(description = "위도")
        @NotBlank double latitude,

        @Schema(description = "경도")
        @NotBlank double longitude,

        @Schema(description = "저작권")
        @NotBlank String copyright
) {
}
