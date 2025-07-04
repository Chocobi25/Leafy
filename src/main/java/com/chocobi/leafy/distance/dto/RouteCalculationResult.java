package com.chocobi.leafy.distance.dto;

import lombok.Data;

@Data
public class RouteCalculationResult {

    private int pathType;
    private int totalTime;

    private int busDistance = 0;
    private int subwayDistance = 0;
    private int trainDistance = 0;
}
