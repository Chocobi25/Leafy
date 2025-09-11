package com.chocobi.leafy.distance.dto;

import com.chocobi.leafy.distance.domain.DistanceResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class CarDistanceResponse {
    private DistanceResponse distanceResponse;
    private List<Section> sections;
}
