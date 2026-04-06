package com.chocobi.leafy.post.dto.response;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private Integer rating;
    private Integer likes;

    private Long userId;
    private String userNickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(PostEntity post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .rating(post.getRating())
                .likes(post.getLikes())
                .userId(post.getUser().getId())
                .userNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
