package com.chocobi.leafy.external.tour.dto;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import lombok.Getter;

@Getter
public class TourImageResponse {
    private ExternalApiResponse<TourImageItem> externalApiResponse;

    @Getter
    public static class TourImageItem {
        private String galTitle;
        private String galWebImageUrl;
        private String galPhotographyLocation;
        private String galPhotographer;
    }
}
