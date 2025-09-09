package com.chocobi.leafy.place.fetcher.rural.dto;

import lombok.Data;

@Data
public class RuralItem {
    String title;        // 제목
    String reference;    // 번호
    String rights;       // 저작권
    String source;       // 사이트 URL
    String description;  // 내용
    String spatial;      // 경도, 위도
    String affiliation;  // 주소
}
