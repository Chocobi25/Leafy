package com.chocobi.leafy.post.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseEntity {
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long placeId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private Integer likes = 0;

    @Builder
    public PostEntity(String title, String content, User user, Long placeId) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.placeId = placeId;
        this.rating = 0;
        this.likes = 0;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public void update(String title, String content, Long placeId) {
        this.title = title;
        this.content = content;
        this.placeId = placeId;
    }
}
