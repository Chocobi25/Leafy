package com.chocobi.leafy.place.common.dto;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.infra.entity.RegionGroup;
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

    public static PlaceDTO fromEntity(PlaceEntity place) {
        Long id = place.getId();
        String title = place.getTitle();
        String address = place.getAddress();
        double lat = place.getLatitude();
        double lng = place.getLongitude();
        String copyright = place.getCopyright();

        if (place instanceof ExternalPlaceEntity external) {
            return new PlaceDTO(
                    id, title,
                    external.getCategory().name(),
                    address, lat, lng,
                    external.getDescription(),
                    external.getTel(),
                    external.getUrl()
            );
        }

        throw new IllegalArgumentException("지원하지 않는 엔티티 타입입니다: " + place.getClass());
    }
}