package com.chocobi.leafy.external.vworld.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class VWorldRegionResponse {
    private AdmVOList admVOList;

    @Getter
    public static class AdmVOList {
        private List<VWorldRegionItem> admVOList;
        private String totalCount;
        private String numOfRows;
        private String error;
        private String message;
    }

    @Getter
    public static class VWorldRegionItem {
        private String admCode;
        private String admCodeNm;
        private String lowestAdmCodeNm;
    }
}