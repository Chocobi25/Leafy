package com.chocobi.leafy.distance.dto;

import lombok.Data;

@Data
public class Summary {

    private int distance;   // 전체 검색 결과 거리(미터)
    private int duration;   // 목적지까지 소요 시간(초)
}
