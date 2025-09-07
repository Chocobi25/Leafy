package com.chocobi.leafy.distance.dto;

import lombok.Data;

@Data
public class TmapResponse {
    private MetaData metaData;
    private Result result;
    
    @Data
    public static class Result {
        private String message;
        private int status;
    }
}