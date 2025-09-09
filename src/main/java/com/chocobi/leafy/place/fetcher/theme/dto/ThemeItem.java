package com.chocobi.leafy.place.fetcher.theme.dto;

import lombok.Data;

@Data
public class ThemeItem {
    private String title;                   // 제목
    private String creator;                 // 저작권
    private String description;             // 설명
    private String spatial;                 // 주소
    private String reference;               // 전화번호
}
