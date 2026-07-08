package com.chocobi.leafy.distance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Section {
    private int distance;
    private int duration; // 단위: 초
    private double carbonEmission;
    private double maxCarbonEmission;
}
