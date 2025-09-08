package com.chocobi.leafy.distance.dto;

import lombok.Data;

@Data
public class RouteCalculationResult {

    private int pathType;
    private int totalTime;
    private double totalDistance;

    private double carbonEmission;
    private double maxCarbonEmission;

    private int busDistance;
    private int subwayDistance;
    private int trainDistance;
    private int airplaneDistance;
}