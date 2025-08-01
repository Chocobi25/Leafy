package com.chocobi.leafy.place.dto;

import com.chocobi.leafy.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlaceDTO {
    private Long id;
    private String title;
    private String category;
    private String address;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String description;
    private String tel;
    private String url;

    public static PlaceDTO fromEntity(Place place){
        return new PlaceDTO(
                place.getId(),
                place.getTitle(),
                place.getCategory().name(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getImageUrl(),
                place.getDescription(),
                place.getTel(),
                place.getUrl()
        );
    }
}