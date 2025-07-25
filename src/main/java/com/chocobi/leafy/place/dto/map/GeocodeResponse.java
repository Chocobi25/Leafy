package com.chocobi.leafy.place.dto.map;

import lombok.Data;

import java.util.List;

@Data
public class GeocodeResponse {
    private List<Document> documents;
}
