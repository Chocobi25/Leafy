package com.chocobi.leafy.place.fetcher.kakao.dto;

import lombok.Data;

@Data
public class Address {
    private String address_name;
    private String region_1depth_name;
    private String region_2depth_name;
    private String region_3depth_name;
}
