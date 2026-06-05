package com.chocobi.leafy.external.tour.dto;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TourKoreanDetailImageResponse {
    @JsonProperty("response")
    private ExternalApiResponse<TourKoreanDetailImageItem> externalApiResponse;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TourKoreanDetailImageItem {
        private String contentid;
        private String imgname;
        private String originimgurl;
        private String serialnum;
        private String smallimageurl;
        private String cpyrhtDivCd;
    }
}
