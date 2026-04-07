package com.chocobi.leafy.post.dto.response;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostListResponse {
    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "좋아요 수")
    private Integer likes;

    @Schema(description = "조회수")
    private Integer viewCount;

    @Schema(description = "작성자 닉네임")
    private String userNickname;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    public static PostListResponse from(PostEntity post) {
        return PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .likes(post.getLikes())
                .viewCount(post.getViewCount())
                .userNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
