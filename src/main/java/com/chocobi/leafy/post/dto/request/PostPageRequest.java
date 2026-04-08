package com.chocobi.leafy.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record PostPageRequest(
        @Schema(description = "페이지 번호 (0부터 시작)", defaultValue = "0")
        @Min(0) int page,

        @Schema(description = "페이지 크기", defaultValue = "10")
        @Min(1) int size
) {
}