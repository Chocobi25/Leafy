package com.chocobi.leafy.place.fetcher.farm;

import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmDetailItem;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {
    PlaceStaging toStaging(FarmDetailItem item) {
        return PlaceStaging.builder()
                .title(item.getCntntsSj())
                .description(item.getSmm())
                .category(Category.FOOD)
                .address(item.getLocplc())
                .tel(item.getTelno())
                .url(item.getUrl())
                .sourceApiName("FarmAPI")
                .build();
    }
}
