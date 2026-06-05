package com.chocobi.leafy.external.tour.dto;

public record TourKoreanPlaceSearchCondition(
        Integer contentTypeId,
        String areaCode,
        String sigunguCode,
        String lclsSystm1,
        String lclsSystm2,
        String lclsSystm3
) {
}
