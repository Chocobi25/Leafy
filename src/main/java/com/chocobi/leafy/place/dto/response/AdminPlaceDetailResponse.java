package com.chocobi.leafy.place.dto.response;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPlaceDetailResponse {
    @Schema(description = "장소 ID")
    private Long id;

    @Schema(description = "장소명")
    private String title;

    @Schema(description = "장소 타입")
    private String placeType;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "카테고리 ID")
    private Long categoryId;

    @Schema(description = "카테고리명")
    private String category;

    @Schema(description = "지역 ID")
    private Long regionId;

    @Schema(description = "지역명")
    private String region;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "위도")
    private double latitude;

    @Schema(description = "경도")
    private double longitude;

    @Schema(description = "전화번호")
    private String tel;

    @Schema(description = "홈페이지 URL")
    private String url;

    @Schema(description = "저작권")
    private String copyright;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static AdminPlaceDetailResponse from(PlaceEntity place) {
        ExternalPlaceEntity externalPlace = place instanceof ExternalPlaceEntity external ? external : null;

        return AdminPlaceDetailResponse.builder()
                .id(place.getId())
                .title(place.getTitle())
                .placeType(externalPlace != null ? "EXTERNAL" : "CUSTOM")
                .description(externalPlace != null ? externalPlace.getDescription() : null)
                .categoryId(externalPlace != null && externalPlace.getCategory() != null
                        ? externalPlace.getCategory().getId()
                        : null)
                .category(externalPlace != null && externalPlace.getCategory() != null
                        ? externalPlace.getCategory().getName()
                        : null)
                .regionId(externalPlace != null && externalPlace.getRegion() != null
                        ? externalPlace.getRegion().getId()
                        : null)
                .region(externalPlace != null && externalPlace.getRegion() != null
                        ? externalPlace.getRegion().getName()
                        : null)
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .tel(externalPlace != null ? externalPlace.getTel() : null)
                .url(externalPlace != null ? externalPlace.getUrl() : null)
                .copyright(place.getCopyright())
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}
