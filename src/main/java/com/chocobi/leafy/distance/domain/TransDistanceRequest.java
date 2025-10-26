package com.chocobi.leafy.distance.domain;

import lombok.Data;

@Data
public class TransDistanceRequest {
    private String startX;
    private String startY;
    private String endX;
    private String endY;
}