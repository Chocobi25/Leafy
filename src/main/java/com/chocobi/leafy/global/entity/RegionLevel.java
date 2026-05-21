package com.chocobi.leafy.global.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegionLevel {
    SIDO(1, "시도"),
    SIGUNGU(2, "시군구"),
    EMD(3, "읍면동"),
    REE(4, "리");

    private final int depth;
    private final String description;
}