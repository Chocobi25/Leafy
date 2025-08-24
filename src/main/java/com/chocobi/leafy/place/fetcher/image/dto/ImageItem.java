package com.chocobi.leafy.place.fetcher.image.dto;

import lombok.Data;

@Data
public class ImageItem {
    private String galTitle;         // 제목
    private String galWebImageUrl;   // 웹용 이미지 경로
    private String galPhotographer;  // 촬영자
}
