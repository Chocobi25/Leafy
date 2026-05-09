package com.chocobi.leafy.place.dto.response;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalPlaceListResponse {
    @Schema(description = "장소 ID")
    private Long id;

    @Schema(description = "장소명")
    private String title;

    @Schema(description = "카테고리")
    private String category;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "지역명")
    private String region;

    public static ExternalPlaceListResponse from(ExternalPlaceEntity place) {
        return ExternalPlaceListResponse.builder()
                .id(place.getId())
                .title(place.getTitle())
                .category(place.getCategory().getName())
                .address(place.getAddress())
                .region(place.getRegion().getName())
                .build();
    }
}
