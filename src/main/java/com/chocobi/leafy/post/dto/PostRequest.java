package com.chocobi.leafy.post.dto;

import lombok.Data;

@Data
public class PostRequest {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long placeId;
    private Integer rating;
}
