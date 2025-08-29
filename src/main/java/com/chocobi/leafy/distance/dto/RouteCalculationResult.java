package com.chocobi.leafy.distance.dto;

import lombok.Data;

@Data
public class RouteCalculationResult {

    private int pathType;
    private int totalTime;
    private double totalDistance;

    private double carbonEmission;

    private int busDistance;
    private int subwayDistance;
    private int trainDistance;
}