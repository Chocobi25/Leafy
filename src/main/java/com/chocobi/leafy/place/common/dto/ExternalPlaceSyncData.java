package com.chocobi.leafy.place.common.dto;

import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import java.util.List;

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
        String version,
        boolean syncImages,
        List<ExternalPlaceImageSyncData> images
) {
}
