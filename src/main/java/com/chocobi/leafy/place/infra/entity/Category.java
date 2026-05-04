package com.chocobi.leafy.place.infra.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
    NATURE("자연"),
    EXPERIENCE("체험"),
    CULTURE("문화"),
    FOOD("음식"),
    ETC("기타");

    private final String label;
}
