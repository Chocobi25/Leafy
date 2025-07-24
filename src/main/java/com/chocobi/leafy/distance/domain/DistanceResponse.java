package com.chocobi.leafy.distance.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistanceResponse {

    private double distance; // 단위: 미터
    private int duration; // 단위: 밀리초
    private double carbonEmission;
}