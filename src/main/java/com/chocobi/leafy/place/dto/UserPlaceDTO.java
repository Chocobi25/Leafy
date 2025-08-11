package com.chocobi.leafy.place.dto;

import lombok.Data;

@Data
public class UserPlaceDTO {
    private String title;
    private String address;
    private double latitude;
    private double longitude;
    private String placeUrl;
    private String tel;
}
