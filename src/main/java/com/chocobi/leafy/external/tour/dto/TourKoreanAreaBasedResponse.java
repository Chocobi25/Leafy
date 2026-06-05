package com.chocobi.leafy.external.tour.dto;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TourKoreanAreaBasedResponse {
    @JsonProperty("response")
    private ExternalApiResponse<TourKoreanAreaBasedItem> externalApiResponse;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TourKoreanAreaBasedItem {
        private String addr1;
        private String addr2;
        private String areacode;
        private String booktour;
        private String cat1;
        private String cat2;
        private String cat3;
        private String contentid;
        private String contenttypeid;
        private String createdtime;
        private String firstimage;
        private String firstimage2;
        private String cpyrhtDivCd;
        private String mapx;
        private String mapy;
        private String mlevel;
        private String modifiedtime;
        private String sigungucode;
        private String tel;
        private String title;
        private String zipcode;
        private String lclsSystm1;
        private String lclsSystm2;
        private String lclsSystm3;
    }
}
