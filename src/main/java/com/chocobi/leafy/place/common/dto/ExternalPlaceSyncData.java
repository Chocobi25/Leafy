package com.chocobi.leafy.place.common.dto;

import com.chocobi.leafy.place.vo.ExternalPlaceSource;

public record ExternalPlaceSyncData(
        ExternalPlaceSource source,
        String categoryCode,
        String contentId,
        Integer contentTypeId,
        String title,
        String address,
        double latitude,
        double longitude,
        String copyright,
        String description,
        String tel,
        String url,
        String largeCategoryCode,
        String middleCategoryCode,
        String smallCategoryCode,
        String version
) {
}
