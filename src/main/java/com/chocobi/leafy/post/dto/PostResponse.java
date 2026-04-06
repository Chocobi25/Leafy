package com.chocobi.leafy.post.dto;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private UserInfo user;
    private Long placeId;
    private Integer rating;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long kakaoId;
        private String nickname;
        private String profileImageUrl;
    }

    public static PostResponse fromEntity(PostEntity post) {
        UserInfo userInfo = null;
        if (post.getUser() != null) {
            userInfo = new UserInfo(
                    post.getUser().getId(),  // TODO: 로직 동작 확인
                    post.getUser().getNickname(),
                    post.getUser().getProfileImageUrl()
            );
        }
  
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                userInfo,
                post.getPlaceId(),
                post.getRating(),
                post.getLikes(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
