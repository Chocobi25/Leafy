package com.chocobi.leafy.external.tour.dto;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TourEcoTourismResponse {
    @JsonProperty("response")
    private ExternalApiResponse<TourEcoTourismItem> externalApiResponse;

    @Getter
    public static class TourEcoTourismItem {
        private String cpyrhtDivCd;
        private String contentid;
        private String title;
        private String subtitle;
        private String summary;
        private String addr;
        private String telname;
        private String tel;
        private String mainimage;
        private String showflag;
        private String sigungucode;
        private String createdtime;
        private String modifiedtime;
    }
}
