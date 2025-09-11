package com.chocobi.leafy.place.fetcher.kakao.dto;

import lombok.Data;

@Data
public class GeocodeResult {
    private final double latitude;
    private final double longitude;
    private final String address;
}
