package com.chocobi.leafy.distance.domain;

import lombok.Data;

import java.util.List;

@Data
public class TransDistanceBatchRequest {
    private Long tripId;
    private List<TransDistanceRequest> requests;
}
