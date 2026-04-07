package com.chocobi.leafy.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
        @Schema(description = "제목")
        @NotBlank String title,

        @Schema(description = "내용")
        @NotBlank String content,

        @Schema(description = "작성자 ID")
        Long userId,

        @Schema(description = "장소 ID")
        Long placeId
) {}
