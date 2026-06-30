package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.external.tour.dto.TourKoreanPlaceSearchCondition;

public enum TourPlaceCollectionTarget {
    NATURE("NA", null, null, "NATURE", 1),
    RURAL_EXPERIENCE(null, "EX03", null, "EXPERIENCE", 2),
    TEMPLE_EXPERIENCE(null, "EX04", null, "EXPERIENCE", 2),
    WELLNESS(null, "EX05", null, "EXPERIENCE", 2),
    CITY_PARK(null, "VE03", null, "NATURE", 1),
    TRAIL(null, null, "VE040300", "NATURE", 1),
    BICYCLE(null, null, "LS010200", "EXPERIENCE", 2),
    CAMPING(null, "AC05", null, "ETC", 3),
    RURAL_LODGING(null, null, "AC030300", "ETC", 3);

    private final String largeCategoryCode;
    private final String middleCategoryCode;
    private final String smallCategoryCode;
    private final String categoryCode;
    private final int categoryPriority;

    TourPlaceCollectionTarget(
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            String categoryCode,
            int categoryPriority
    ) {
        this.largeCategoryCode = largeCategoryCode;
        this.middleCategoryCode = middleCategoryCode;
        this.smallCategoryCode = smallCategoryCode;
        this.categoryCode = categoryCode;
        this.categoryPriority = categoryPriority;
    }

    public TourKoreanPlaceSearchCondition condition() {
        return new TourKoreanPlaceSearchCondition(
                null,
                null,
                null,
                largeCategoryCode,
                middleCategoryCode,
                smallCategoryCode
        );
    }

    public String categoryCode() {
        return categoryCode;
    }

    public int categoryPriority() {
        return categoryPriority;
    }
}
