package com.chocobi.leafy.place.fetcher.kakao.dto;

import lombok.Data;

@Data
public class Document {
    private Address address;        // 주소
    private String x;               // 경도
    private String y;               // 위도
}
