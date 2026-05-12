package com.chocobi.leafy.place.dto.request;

import com.chocobi.leafy.place.vo.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record AdminPlacePageRequest(
        @Schema(description = "페이지 번호 (1부터 시작)", defaultValue = "1")
        @Min(1) Integer page,

        @Schema(description = "페이지 크기", defaultValue = "10")
        @Min(1) Integer size,

        @Schema(description = "지역 ID")
        Long regionId,

        @Schema(description = "카테고리 ID")
        Long categoryId,

        @Schema(description = "장소 타입 (EXTERNAL, CUSTOM)")
        PlaceType placeType
) {
    public AdminPlacePageRequest {
        page = page == null ? 1 : page;
        size = size == null ? 10 : size;
    }
}
