package com.chocobi.leafy.distance.dto;

import lombok.Data;

@Data
public class Section {
    private int distance;
    private int duration; // 단위: 초
    private double carbonEmission;
}
