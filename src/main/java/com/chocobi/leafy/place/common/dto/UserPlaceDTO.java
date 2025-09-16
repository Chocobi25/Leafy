package com.chocobi.leafy.place.common.dto;

import lombok.Data;

@Data
public class UserPlaceDTO {
    private String title;
    private String tel;
    private String address;
    private double longitude;
    private double latitude;
    private String url;
}
