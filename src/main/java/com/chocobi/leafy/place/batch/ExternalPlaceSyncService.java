package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.common.dto.ExternalPlaceSyncData;
import com.chocobi.leafy.place.common.dto.ExternalPlaceImageSyncData;
import com.chocobi.leafy.place.infra.CategoryFindService;
import com.chocobi.leafy.place.infra.ExternalPlaceCommandService;
import com.chocobi.leafy.place.infra.ExternalPlaceFindService;
import com.chocobi.leafy.place.infra.PlaceImageCommandService;
import com.chocobi.leafy.place.infra.PlaceImageFindService;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalPlaceSyncService {
    private final ExternalPlaceFindService externalPlaceFindService;
    private final ExternalPlaceCommandService externalPlaceCommandService;
    private final CategoryFindService categoryFindService;
    private final PlaceImageFindService placeImageFindService;
    private final PlaceImageCommandService placeImageCommandService;

    public void syncBatch(List<ExternalPlaceSyncData> places) {
        if (places.isEmpty()) {
            return;
        }

        ExternalPlaceSource source = places.getFirst().source();
        validateSingleSource(places, source);
        Map<String, CategoryEntity> categories = findCategories(places);
        Map<String, ExternalPlaceEntity> existingPlaces = findExistingPlaces(places, source);
        List<ExternalPlaceEntity> placesToSave = new ArrayList<>();
        Map<String, ExternalPlaceEntity> synchronizedPlaces = new HashMap<>();

        for (ExternalPlaceSyncData place : places) {
            CategoryEntity category = categories.get(place.categoryCode());
            ExternalPlaceEntity existingPlace = existingPlaces.get(place.contentId());
            if (existingPlace == null) {
                ExternalPlaceEntity newPlace = createPlace(place, category);
                placesToSave.add(newPlace);
                synchronizedPlaces.put(place.contentId(), newPlace);
                continue;
            }
            if (existingPlace.needsSync(place.version(), category)) {
                existingPlace.sync(createPlace(place, category));
                placesToSave.add(existingPlace);
            }
            synchronizedPlaces.put(place.contentId(), existingPlace);
        }
        if (!placesToSave.isEmpty()) {
            externalPlaceCommandService.saveAll(placesToSave);
        }
        syncImages(places, synchronizedPlaces);
    }

    private void syncImages(
            List<ExternalPlaceSyncData> places,
            Map<String, ExternalPlaceEntity> synchronizedPlaces
    ) {
        List<ExternalPlaceSyncData> imageTargets = places.stream()
                .filter(ExternalPlaceSyncData::syncImages)
                .toList();
        if (imageTargets.isEmpty()) {
            return;
        }

        List<ExternalPlaceEntity> targetPlaces = imageTargets.stream()
                .map(place -> synchronizedPlaces.get(place.contentId()))
                .toList();
        Map<Long, List<PlaceImageEntity>> existingImages = placeImageFindService.findPlaceImages(targetPlaces)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getPlace().getId()));
        List<ExternalPlaceEntity> placesToReplace = new ArrayList<>();
        List<PlaceImageEntity> imagesToSave = new ArrayList<>();

        for (ExternalPlaceSyncData data : imageTargets) {
            ExternalPlaceEntity place = synchronizedPlaces.get(data.contentId());
            List<PlaceImageEntity> currentImages = existingImages.getOrDefault(place.getId(), List.of());
            if (hasSameImages(currentImages, data.images())) {
                continue;
            }
            placesToReplace.add(place);
            data.images().stream()
                    .map(image -> createImage(image, place))
                    .forEach(imagesToSave::add);
        }
        placeImageCommandService.replaceAll(placesToReplace, imagesToSave);
    }

    private boolean hasSameImages(
            List<PlaceImageEntity> currentImages,
            List<ExternalPlaceImageSyncData> syncImages
    ) {
        if (currentImages.size() != syncImages.size()) {
            return false;
        }
        List<PlaceImageEntity> sortedCurrentImages = currentImages.stream()
                .sorted(Comparator.comparing(
                        PlaceImageEntity::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<ExternalPlaceImageSyncData> sortedSyncImages = syncImages.stream()
                .sorted(Comparator.comparingInt(ExternalPlaceImageSyncData::sortOrder))
                .toList();
        for (int index = 0; index < sortedCurrentImages.size(); index++) {
            PlaceImageEntity current = sortedCurrentImages.get(index);
            ExternalPlaceImageSyncData sync = sortedSyncImages.get(index);
            if (!Objects.equals(current.getUrl(), sync.url())
                    || !Objects.equals(current.getSource(), sync.source())
                    || !Objects.equals(current.getSortOrder(), sync.sortOrder())
                    || !Objects.equals(current.getThumbnail(), sync.thumbnail())) {
                return false;
            }
        }
        return true;
    }

    private PlaceImageEntity createImage(
            ExternalPlaceImageSyncData image,
            ExternalPlaceEntity place
    ) {
        return PlaceImageEntity.builder()
                .url(image.url())
                .source(image.source())
                .sortOrder(image.sortOrder())
                .thumbnail(image.thumbnail())
                .place(place)
                .build();
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
