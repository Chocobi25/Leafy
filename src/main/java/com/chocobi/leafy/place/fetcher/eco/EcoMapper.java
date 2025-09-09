package com.chocobi.leafy.place.fetcher.eco;

import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.eco.dto.EcoItem;
import org.springframework.stereotype.Component;

@Component
public class EcoMapper {
    public PlaceStaging toStaging(EcoItem item) {
        return PlaceStaging.builder()
                .title(item.getTitle())
                .description(item.getSummary())
                .category(Category.NATURE)
                .address(item.getAddr())
                .tel(item.getTel())
                .copyright("한국관광공사")
                .sourceApiName("EcoAPI")
                .build();
    }
}

