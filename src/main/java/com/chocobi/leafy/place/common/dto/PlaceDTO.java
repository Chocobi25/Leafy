package com.chocobi.leafy.place.common.dto;

import com.chocobi.leafy.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlaceDTO {
    private Long id;
    private String title;
    private String category;
    private String address;
    private double latitude;
    private double longitude;
    private String description;
    private String tel;
    private String url;
    private List<ImageDTO> images;

    public static PlaceDTO fromEntity(Place place){
        return new PlaceDTO(
                place.getId(),
                place.getTitle(),
                place.getCategory().name(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getDescription(),
                place.getTel(),
                place.getUrl(),
                place.getImages() != null ?
                        place.getImages().stream()
                                .map(ImageDTO::fromEntity)
                                .toList()
                        : List.of()
        );
    }
}