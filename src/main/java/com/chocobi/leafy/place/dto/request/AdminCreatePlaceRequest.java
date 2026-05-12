package com.chocobi.leafy.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreatePlaceRequest(
        @Schema(description = "장소명")
        @NotBlank String title,

        @Schema(description = "설명")
        String description,

        @Schema(description = "카테고리 ID")
        @NotNull Long categoryId,

        @Schema(description = "지역 ID")
        @NotNull Long regionId,

        @Schema(description = "주소")
        @NotBlank String address,

        @Schema(description = "위도")
        @NotNull Double latitude,

        @Schema(description = "경도")
        @NotNull Double longitude,

        @Schema(description = "전화번호")
        String tel,

        @Schema(description = "홈페이지 URL")
        String url,

        @Schema(description = "저작권")
        @NotBlank String copyright
) {
}
