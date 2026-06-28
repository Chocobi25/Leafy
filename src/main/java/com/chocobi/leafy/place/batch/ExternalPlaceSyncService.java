package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.common.dto.ExternalPlaceSyncData;
import com.chocobi.leafy.place.infra.CategoryFindService;
import com.chocobi.leafy.place.infra.ExternalPlaceCommandService;
import com.chocobi.leafy.place.infra.ExternalPlaceFindService;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalPlaceSyncService {
    private final ExternalPlaceFindService externalPlaceFindService;
    private final ExternalPlaceCommandService externalPlaceCommandService;
    private final CategoryFindService categoryFindService;

    public void syncBatch(List<ExternalPlaceSyncData> places) {
        if (places.isEmpty()) {
            return;
        }

        ExternalPlaceSource source = places.getFirst().source();
        validateSingleSource(places, source);
        Map<String, CategoryEntity> categories = findCategories(places);
        Map<String, ExternalPlaceEntity> existingPlaces = findExistingPlaces(places, source);
        List<ExternalPlaceEntity> placesToSave = new ArrayList<>();

        for (ExternalPlaceSyncData place : places) {
            CategoryEntity category = categories.get(place.categoryCode());
            ExternalPlaceEntity existingPlace = existingPlaces.get(place.contentId());
            if (existingPlace == null) {
                placesToSave.add(createPlace(place, category));
                continue;
            }
            if (existingPlace.needsSync(place.version(), category)) {
                existingPlace.sync(createPlace(place, category));
                placesToSave.add(existingPlace);
            }
        }
        if (!placesToSave.isEmpty()) {
            externalPlaceCommandService.saveAll(placesToSave);
        }
    }

    private void validateSingleSource(List<ExternalPlaceSyncData> places, ExternalPlaceSource source) {
        boolean hasDifferentSource = places.stream()
                .anyMatch(place -> place.source() != source);
        if (hasDifferentSource) {
            throw new IllegalArgumentException("서로 다른 외부 장소 공급자를 한 배치에서 동기화할 수 없습니다.");
        }
    }

    private Map<String, CategoryEntity> findCategories(List<ExternalPlaceSyncData> places) {
        Set<String> categoryCodes = places.stream()
                .map(ExternalPlaceSyncData::categoryCode)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, CategoryEntity> categories = new HashMap<>();
        categoryFindService.findCategories(categoryCodes)
                .forEach(category -> categories.put(category.getCode(), category));

        if (!categories.keySet().containsAll(categoryCodes)) {
            throw new IllegalStateException("장소 카테고리가 존재하지 않습니다. codes=" + categoryCodes);
        }
        return categories;
    }

    public int deactivateMissing(ExternalPlaceSource source, Collection<String> collectedContentIds) {
        Set<String> collectedIds = new HashSet<>(collectedContentIds);
        List<String> missingIds = externalPlaceFindService.findActiveExternalContentIds(source)
                .stream()
                .filter(contentId -> !collectedIds.contains(contentId))
                .toList();

        if (missingIds.isEmpty()) {
            return 0;
        }

        int deactivatedCount = 0;
        for (int start = 0; start < missingIds.size(); start += 500) {
            int end = Math.min(start + 500, missingIds.size());
            List<ExternalPlaceEntity> missingPlaces = externalPlaceFindService
                    .findExternalPlaces(source, missingIds.subList(start, end));
            missingPlaces.forEach(ExternalPlaceEntity::deactivate);
            externalPlaceCommandService.saveAll(missingPlaces);
            deactivatedCount += missingPlaces.size();
        }
        return deactivatedCount;
    }

    private Map<String, ExternalPlaceEntity> findExistingPlaces(
            List<ExternalPlaceSyncData> places,
            ExternalPlaceSource source
    ) {
        List<String> contentIds = places.stream().map(ExternalPlaceSyncData::contentId).toList();
        Map<String, ExternalPlaceEntity> result = new HashMap<>();
        externalPlaceFindService.findExternalPlaces(source, contentIds)
                .forEach(place -> result.put(place.getExternalContentId(), place));
        return result;
    }

    private ExternalPlaceEntity createPlace(ExternalPlaceSyncData data, CategoryEntity category) {
        return ExternalPlaceEntity.builder()
                .externalContentId(data.contentId())
                .source(data.source())
                .contentTypeId(data.contentTypeId())
                .title(data.title())
                .address(data.address())
                .latitude(data.latitude())
                .longitude(data.longitude())
                .copyright(data.copyright())
                .description(data.description())
                .category(category)
                .tel(data.tel())
                .url(data.url())
                .largeCategoryCode(data.largeCategoryCode())
                .middleCategoryCode(data.middleCategoryCode())
                .smallCategoryCode(data.smallCategoryCode())
                .externalVersion(data.version())
                .status(ExternalPlaceStatus.ACTIVE)
                .build();
    }
}
