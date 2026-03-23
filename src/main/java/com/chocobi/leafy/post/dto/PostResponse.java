package com.chocobi.leafy.post.dto;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.post.entity.Post;
import com.chocobi.leafy.user.entity.User;
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
    private PlaceInfo place;
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

    @Data
    @AllArgsConstructor
    public static class PlaceInfo {
        private Long id;
        private String title;
        private String category;
        private String address;
    }

    public static PostResponse fromEntity(Post post) {
        UserInfo userInfo = null;
        if (post.getUser() != null) {
            userInfo = new UserInfo(
                    post.getUser().getId(),  // TODO: 로직 동작 확인
                    post.getUser().getNickname(),
                    post.getUser().getProfileImageUrl()
            );
        }

        PlaceInfo placeInfo = null;
        if (post.getPlace() != null) {
            placeInfo = new PlaceInfo(
                    post.getPlace().getId(),
                    post.getPlace().getTitle(),
                    post.getPlace().getCategory().name(),
                    post.getPlace().getAddress()
            );
        }
  
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                userInfo,
                placeInfo,
                post.getRating(),
                post.getLikes(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
