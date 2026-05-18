package com.chocobi.leafy.place.common.dto;

import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageDTO {
    private Long id;
    private String url;
    private String source;

    public static ImageDTO fromEntity(PlaceImageEntity placeImageEntity) {
        return new ImageDTO(
                placeImageEntity.getId(),
                placeImageEntity.getUrl(),
                placeImageEntity.getSource()
        );
    }
}
