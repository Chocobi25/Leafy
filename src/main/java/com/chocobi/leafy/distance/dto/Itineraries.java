package com.chocobi.leafy.distance.dto;

import lombok.Data;

import java.util.List;

@Data
public class Itineraries {
    private int totalTime;
    private int totalDistance;
    private int totalWalkDistance;
    private int pathType;
    private List<Legs> legs;
}
