package com.chocobi.leafy.distance.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CarDistanceRequest {
    private Long tripId;
    private Point origin;
    private Point destination;
    private List<Point> waypoints = new ArrayList<>();
}
