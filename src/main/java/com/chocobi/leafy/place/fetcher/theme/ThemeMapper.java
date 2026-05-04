package com.chocobi.leafy.place.fetcher.theme;

import com.chocobi.leafy.place.infra.entity.Category;
import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.theme.dto.ThemeItem;
import org.springframework.stereotype.Component;

@Component
public class ThemeMapper {
    PlaceStaging toStaging(ThemeItem item) {
        return PlaceStaging.builder()
                .title(item.getTitle())
                .description(item.getDescription())
                .tel(item.getReference())
                .category(Category.CULTURE)
                .copyright(item.getCreator())
                .address(item.getSpatial())
                .sourceApiName("ThemeAPI")
                .build();
    }
}
