package com.chocobi.leafy.place.fetcher.kakao.dto;

import lombok.Data;

import java.util.List;

@Data
public class GeocodeResponse {
    private List<Document> documents;
}
