package com.chocobi.leafy.place.dto.response;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPlaceListResponse {
    @Schema(description = "장소 ID")
    private Long id;

    @Schema(description = "장소명")
    private String title;

    @Schema(description = "장소 타입")
    private String placeType;

    @Schema(description = "카테고리")
    private String category;

    @Schema(description = "지역명")
    private String region;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static AdminPlaceListResponse from(PlaceEntity place) {
        ExternalPlaceEntity externalPlace = place instanceof ExternalPlaceEntity external ? external : null;

        return AdminPlaceListResponse.builder()
                .id(place.getId())
                .title(place.getTitle())
                .placeType(externalPlace != null ? "EXTERNAL" : "CUSTOM")
                .category(externalPlace != null && externalPlace.getCategory() != null
                        ? externalPlace.getCategory().getName()
                        : null)
                .region(externalPlace != null && externalPlace.getRegion() != null
                        ? externalPlace.getRegion().getName()
                        : null)
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}
