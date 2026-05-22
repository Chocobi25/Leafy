package com.chocobi.leafy.external.kakao.dto;

import lombok.Data;

import java.util.List;

@Data
public class GeocodeResponse {
    private List<Document> documents;

    @Data
    public static class Document {
        private Address address;
        private String x;
        private String y;
    }

    @Data
    public static class Address {
        private String address_name;
        private String region_1depth_name;
        private String region_2depth_name;
        private String region_3depth_name;
    }
}
