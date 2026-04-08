package com.chocobi.leafy.post.dto.response;

import com.chocobi.leafy.post.infra.entity.PostCommentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostCommentResponse {
    @Schema(description = "댓글 ID")
    private Long id;

    @Schema(description = "부모 댓글 ID (null이면 최상위 댓글)")
    private Long parentId;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "작성자 ID")
    private Long userId;

    @Schema(description = "작성자 닉네임")
    private String userNickname;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    public static PostCommentResponse from(PostCommentEntity comment) {
        return PostCommentResponse.builder()
                .id(comment.getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}