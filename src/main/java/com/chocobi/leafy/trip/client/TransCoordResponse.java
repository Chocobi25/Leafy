package com.chocobi.leafy.trip.client;

import lombok.Data;

import java.util.List;

@Data
public class TransCoordResponse {
    private List<TransDocument> documents;
}
