package com.chocobi.leafy.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostUpdateRequest(
        @Schema(description = "게시글 ID")
        @NotNull Long postId,

        @Schema(description = "제목")
        @NotBlank String title,

        @Schema(description = "내용")
        @NotBlank String content,

        @Schema(description = "장소 ID (null이면 장소 없음)")
        Long placeId
) {}
