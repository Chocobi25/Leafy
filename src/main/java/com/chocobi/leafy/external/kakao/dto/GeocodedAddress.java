package com.chocobi.leafy.external.kakao.dto;

import com.chocobi.leafy.external.kakao.dto.GeocodeResponse.Address;
import lombok.Data;

@Data
public class GeocodedAddress {
    private final double latitude;
    private final double longitude;
    private final Address address;
}
