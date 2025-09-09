package com.chocobi.leafy.place.fetcher.rural;

import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.rural.dto.RuralItem;
import org.springframework.stereotype.Component;

@Component
public class RuralMapper {
    PlaceStaging toStaging(RuralItem item) {
        return PlaceStaging.builder()
                .title(item.getTitle())
                .description(item.getDescription())
                .category(Category.EXPERIENCE)
                .address(item.getAffiliation())
                .tel(item.getReference())
                .url(item.getSource())
                .copyright(item.getRights())
                .sourceApiName("RuralAPI")
                .build();
    }
}
