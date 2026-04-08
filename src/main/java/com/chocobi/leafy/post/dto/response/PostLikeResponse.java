package com.chocobi.leafy.post.dto.response;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostLikeResponse {

    @Schema(description = "게시글 ID")
    private Long postId;

    @Schema(description = "좋아요 수")
    private Integer likes;

    @Schema(description = "좋아요 여부")
    private boolean liked;

    public static PostLikeResponse of(PostEntity post, boolean liked) {
        return PostLikeResponse.builder()
                .postId(post.getId())
                .likes(post.getLikes())
                .liked(liked)
                .build();
    }
}
