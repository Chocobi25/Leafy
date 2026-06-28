package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.external.tour.dto.TourKoreanPlaceSearchCondition;
import com.chocobi.leafy.place.infra.entity.Category;

public enum TourPlaceCollectionTarget {
    NATURE("NA", null, null, Category.NATURE, 1),
    RURAL_EXPERIENCE(null, "EX03", null, Category.EXPERIENCE, 2),
    TEMPLE_EXPERIENCE(null, "EX04", null, Category.EXPERIENCE, 2),
    WELLNESS(null, "EX05", null, Category.EXPERIENCE, 2),
    CITY_PARK(null, "VE03", null, Category.NATURE, 1),
    TRAIL(null, null, "VE040300", Category.NATURE, 1),
    BICYCLE(null, null, "LS010200", Category.EXPERIENCE, 2),
    CAMPING(null, "AC05", null, Category.ETC, 3),
    RURAL_LODGING(null, null, "AC030300", Category.ETC, 3);

    private final String largeCategoryCode;
    private final String middleCategoryCode;
    private final String smallCategoryCode;
    private final Category category;
    private final int categoryPriority;

    TourPlaceCollectionTarget(
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            Category category,
            int categoryPriority
    ) {
        this.largeCategoryCode = largeCategoryCode;
        this.middleCategoryCode = middleCategoryCode;
        this.smallCategoryCode = smallCategoryCode;
        this.category = category;
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
        return category.name();
    }

    public int categoryPriority() {
        return categoryPriority;
    }
}
