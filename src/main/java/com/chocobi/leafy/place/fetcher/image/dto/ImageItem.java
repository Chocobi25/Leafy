package com.chocobi.leafy.place.fetcher.image.dto;

import lombok.Data;

@Data
public class ImageItem {
    private String title;       // 문서 제목 (저작권)
    private String link;        // 이미지 url
    private String thumbnail;   // 썸네일 url
}
