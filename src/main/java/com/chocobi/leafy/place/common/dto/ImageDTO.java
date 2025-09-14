package com.chocobi.leafy.place.common.dto;

import com.chocobi.leafy.place.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageDTO {
    private Long id;
    private String url;
    private String copyright;

    public static ImageDTO fromEntity(Image image) {
        return new ImageDTO(
                image.getId(),
                image.getUrl(),
                image.getCopyright()
        );
    }
}
