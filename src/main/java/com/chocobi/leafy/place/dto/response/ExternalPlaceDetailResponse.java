package com.chocobi.leafy.place.dto.response;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalPlaceDetailResponse {
    @Schema(description = "장소 ID")
    private Long id;

    @Schema(description = "장소명")
    private String title;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "카테고리")
    private String category;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "지역")
    private String region;

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

    public static ExternalPlaceDetailResponse from(ExternalPlaceEntity place) {
        return ExternalPlaceDetailResponse.builder()
                .id(place.getId())
                .title(place.getTitle())
                .description(place.getDescription())
                .category(place.getCategory() != null ? place.getCategory().getName() : null)
                .address(place.getAddress())
                .region(place.getRegion() != null ? place.getRegion().getName() : null)
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .tel(place.getTel())
                .url(place.getUrl())
                .copyright(place.getCopyright())
                .build();
    }
}
