package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.Category;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.kakao.GeocodeService;
import com.chocobi.leafy.place.fetcher.kakao.dto.GeocodeResult;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceGeocodeProcessor implements ItemProcessor<PlaceStaging, ExternalPlaceEntity> {
    private final GeocodeService geocodeService;
    private final EntityManager entityManager;

    @Override
    public ExternalPlaceEntity process(PlaceStaging item) throws Exception {
        GeocodeResult geocodeResult = geocodeService.getCoordinatesFromAddress(item.getAddress());

        if (geocodeResult.getAddress() == null) {
            log.warn("Failed to get coordinates for address: {}. Skipping item.", item.getAddress());
            return null;
        }

        return ExternalPlaceEntity.builder()
                .title(item.getTitle())
                .address(geocodeResult.getAddress().getAddress_name())
                .latitude(geocodeResult.getLatitude())
                .longitude(geocodeResult.getLongitude())
                .copyright(item.getCopyright())
                .description(item.getDescription())
                .category(findCategory(item.getCategory()))
                .tel(item.getTel())
                .url(item.getUrl())
                .build();
    }

    private CategoryEntity findCategory(Category category) {
        if (category == null) {
            return null;
        }

        List<CategoryEntity> categories = entityManager
                .createQuery("select c from CategoryEntity c where c.code = :code", CategoryEntity.class)
                .setParameter("code", category.name())
                .getResultList();

        return categories.isEmpty() ? null : categories.get(0);
    }
}
