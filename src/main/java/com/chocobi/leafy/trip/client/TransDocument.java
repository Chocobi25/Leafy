package com.chocobi.leafy.trip.client;

import com.chocobi.leafy.external.kakao.dto.GeocodeResponse.Address;
import lombok.Data;

@Data
public class TransDocument {
    public Address address;
}
