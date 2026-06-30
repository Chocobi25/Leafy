package com.chocobi.leafy.place.common.dto;

public record ExternalPlaceImageSyncData(
        String url,
        String source,
        int sortOrder,
        boolean thumbnail
) {
}
