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
    private User user;
    private Place place;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static PostResponse fromEntity(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser(),
                post.getPlace() != null ? post.getPlace() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
