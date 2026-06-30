package com.chocobi.leafy.place.application;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import com.chocobi.leafy.external.tour.client.TourKoreanInfoClient;
import com.chocobi.leafy.external.tour.dto.TourKoreanAreaBasedResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanAreaBasedResponse.TourKoreanAreaBasedItem;
import com.chocobi.leafy.external.tour.dto.TourKoreanDetailImageResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanDetailImageResponse.TourKoreanDetailImageItem;
import com.chocobi.leafy.place.batch.TourPlaceCollectionTarget;
import com.chocobi.leafy.place.common.dto.ExternalPlaceImageSyncData;
import com.chocobi.leafy.place.common.dto.ExternalPlaceSyncData;
import com.chocobi.leafy.place.batch.ExternalPlaceSyncService;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourPlaceSynchronizer implements ExternalPlaceSynchronizer {
    private static final int PAGE_SIZE = 100;
    private static final int SYNC_BATCH_SIZE = 100;
    private static final String SUCCESS_RESULT_CODE = "0000";

    private final TourKoreanInfoClient tourKoreanInfoClient;
    private final ExternalPlaceSyncService externalPlaceSyncService;

    @Override
    public int sync() {
        Map<String, PrioritizedPlace> collectedPlaces = new HashMap<>();
        Set<String> observedContentIds = new HashSet<>();

        for (TourPlaceCollectionTarget target : TourPlaceCollectionTarget.values()) {
            collectTarget(target, collectedPlaces, observedContentIds);
        }

        collectImages(collectedPlaces);
        syncCollectedPlaces(collectedPlaces);
        int deactivatedCount = externalPlaceSyncService.deactivateMissing(
                ExternalPlaceSource.TOUR_API,
                observedContentIds);
        log.info("Tour place sync completed. collected={}, deactivated={}",
                collectedPlaces.size(), deactivatedCount);
        return collectedPlaces.size();
    }

    private void collectTarget(
            TourPlaceCollectionTarget target,
            Map<String, PrioritizedPlace> collectedPlaces,
            Set<String> observedContentIds
    ) {
        int pageNo = 1;
        int fetchedCount = 0;
        int totalCount = Integer.MAX_VALUE;

        while (fetchedCount < totalCount) {
            TourKoreanAreaBasedResponse response = tourKoreanInfoClient.fetchAreaBasedPlaces(
                    target.condition(), pageNo, PAGE_SIZE);
            ExternalApiResponse.Body<TourKoreanAreaBasedItem> body = validateResponse(response, target, pageNo);
            validatePageMetadata(body, target, pageNo);
            totalCount = body.getTotalCount();

            if (pageNo == 1 && totalCount == 0) {
                throw new IllegalStateException("Tour 장소 수집 결과가 없습니다. target=" + target);
            }

            List<TourKoreanAreaBasedItem> items = getItems(body);
            if (items.isEmpty() && fetchedCount < totalCount) {
                throw invalidResponse(target, pageNo, "empty page before totalCount");
            }
            items.stream()
                    .map(TourKoreanAreaBasedItem::getContentid)
                    .filter(contentId -> !isBlank(contentId))
                    .forEach(observedContentIds::add);
            items.stream()
                    .map(item -> mapPlace(item, target))
                    .filter(Objects::nonNull)
                    .forEach(place -> mergePlace(collectedPlaces, place, target.categoryPriority()));

            log.info("Collected Tour places. target={}, page={}, fetched={}, total={}",
                    target, pageNo, items.size(), totalCount);
            fetchedCount += items.size();
            pageNo = body.getPageNo() + 1;
        }
    }

    private void validatePageMetadata(
            ExternalApiResponse.Body<TourKoreanAreaBasedItem> body,
            TourPlaceCollectionTarget target,
            int requestedPageNo
    ) {
        if (body.getPageNo() != requestedPageNo || body.getNumOfRows() <= 0) {
            throw invalidResponse(target, requestedPageNo,
                    "invalid page metadata: pageNo=" + body.getPageNo() + ", numOfRows=" + body.getNumOfRows());
        }
    }

    private void syncCollectedPlaces(Map<String, PrioritizedPlace> collectedPlaces) {
        List<ExternalPlaceSyncData> places = new ArrayList<>(collectedPlaces.size());
        collectedPlaces.values().stream()
                .map(PrioritizedPlace::place)
                .forEach(places::add);

        for (int start = 0; start < places.size(); start += SYNC_BATCH_SIZE) {
            int end = Math.min(start + SYNC_BATCH_SIZE, places.size());
            externalPlaceSyncService.syncBatch(places.subList(start, end));
        }
    }

    private void mergePlace(
            Map<String, PrioritizedPlace> collectedPlaces,
            ExternalPlaceSyncData place,
            int categoryPriority
    ) {
        collectedPlaces.merge(
                place.contentId(),
                new PrioritizedPlace(place, categoryPriority),
                (current, candidate) -> candidate.categoryPriority() < current.categoryPriority()
                        ? candidate
                        : current
        );
    }

    private ExternalApiResponse.Body<TourKoreanAreaBasedItem> validateResponse(
            TourKoreanAreaBasedResponse response,
            TourPlaceCollectionTarget target,
            int pageNo
    ) {
        if (response == null || response.getExternalApiResponse() == null) {
            throw invalidResponse(target, pageNo, "empty response");
        }

        ExternalApiResponse<TourKoreanAreaBasedItem> apiResponse = response.getExternalApiResponse();
        if (apiResponse.getHeader() == null
                || !SUCCESS_RESULT_CODE.equals(apiResponse.getHeader().getResultCode())) {
            String message = apiResponse.getHeader() == null
                    ? "missing header"
                    : apiResponse.getHeader().getResultCode() + ":" + apiResponse.getHeader().getResultMsg();
            throw invalidResponse(target, pageNo, message);
        }
        if (apiResponse.getBody() == null) {
            throw invalidResponse(target, pageNo, "missing body");
        }
        return apiResponse.getBody();
    }

    private List<TourKoreanAreaBasedItem> getItems(ExternalApiResponse.Body<TourKoreanAreaBasedItem> body) {
        if (body.getItems() == null || body.getItems().getItem() == null) {
            return List.of();
        }
        return body.getItems().getItem();
    }

    private ExternalPlaceSyncData mapPlace(
            TourKoreanAreaBasedItem item,
            TourPlaceCollectionTarget target
    ) {
        if (isBlank(item.getContentid()) || isBlank(item.getTitle()) || isBlank(item.getAddr1())
                || isBlank(item.getMapx()) || isBlank(item.getMapy())) {
            log.warn("Skipping invalid Tour place. contentId={}, title={}", item.getContentid(), item.getTitle());
            return null;
        }

        try {
            return new ExternalPlaceSyncData(
                    ExternalPlaceSource.TOUR_API,
                    target.categoryCode(),
                    item.getContentid(),
                    parseInteger(item.getContenttypeid()),
                    item.getTitle(),
                    joinAddress(item.getAddr1(), item.getAddr2()),
                    Double.parseDouble(item.getMapy()),
                    Double.parseDouble(item.getMapx()),
                    item.getCpyrhtDivCd(),
                    null,
                    item.getTel(),
                    null,
                    item.getLclsSystm1(),
                    item.getLclsSystm2(),
                    item.getLclsSystm3(),
                    item.getModifiedtime(),
                    true,
                    thumbnailImage(item)
            );
        } catch (NumberFormatException exception) {
            log.warn("Skipping Tour place with invalid number. contentId={}", item.getContentid(), exception);
            return null;
        }
    }

    private void collectImages(Map<String, PrioritizedPlace> collectedPlaces) {
        collectedPlaces.replaceAll((contentId, prioritizedPlace) -> new PrioritizedPlace(
                withDetailImages(prioritizedPlace.place()),
                prioritizedPlace.categoryPriority()));
    }

    private ExternalPlaceSyncData withDetailImages(ExternalPlaceSyncData place) {
        TourKoreanDetailImageResponse response = tourKoreanInfoClient.fetchDetailImages(place.contentId());
        List<TourKoreanDetailImageItem> detailImages = validateImageResponse(response, place.contentId());
        Map<String, ExternalPlaceImageSyncData> images = new LinkedHashMap<>();
        place.images().forEach(image -> images.put(image.url(), image));

        int sortOrder = images.size();
        for (TourKoreanDetailImageItem image : detailImages) {
            if (isBlank(image.getOriginimgurl()) || images.containsKey(image.getOriginimgurl())) {
                continue;
            }
            images.put(image.getOriginimgurl(), new ExternalPlaceImageSyncData(
                    image.getOriginimgurl(),
                    image.getCpyrhtDivCd(),
                    sortOrder++,
                    false));
        }

        return new ExternalPlaceSyncData(
                place.source(), place.categoryCode(), place.contentId(), place.contentTypeId(),
                place.title(), place.address(), place.latitude(), place.longitude(), place.copyright(),
                place.description(), place.tel(), place.url(), place.largeCategoryCode(),
                place.middleCategoryCode(), place.smallCategoryCode(), place.version(),
                true, List.copyOf(images.values()));
    }

    private List<TourKoreanDetailImageItem> validateImageResponse(
            TourKoreanDetailImageResponse response,
            String contentId
    ) {
        if (response == null || response.getExternalApiResponse() == null) {
            throw new IllegalStateException("Tour 이미지 수집에 실패했습니다. contentId=" + contentId
                    + ", reason=empty response");
        }
        ExternalApiResponse<TourKoreanDetailImageItem> apiResponse = response.getExternalApiResponse();
        if (apiResponse.getHeader() == null
                || !SUCCESS_RESULT_CODE.equals(apiResponse.getHeader().getResultCode())) {
            String reason = apiResponse.getHeader() == null
                    ? "missing header"
                    : apiResponse.getHeader().getResultCode() + ":" + apiResponse.getHeader().getResultMsg();
            throw new IllegalStateException("Tour 이미지 수집에 실패했습니다. contentId=" + contentId
                    + ", reason=" + reason);
        }
        if (apiResponse.getBody() == null
                || apiResponse.getBody().getItems() == null
                || apiResponse.getBody().getItems().getItem() == null) {
            return List.of();
        }
        return apiResponse.getBody().getItems().getItem();
    }

    private List<ExternalPlaceImageSyncData> thumbnailImage(TourKoreanAreaBasedItem item) {
        if (isBlank(item.getFirstimage())) {
            return List.of();
        }
        return List.of(new ExternalPlaceImageSyncData(
                item.getFirstimage(),
                item.getCpyrhtDivCd(),
                0,
                true));
    }

    private IllegalStateException invalidResponse(TourPlaceCollectionTarget target, int pageNo, String message) {
        return new IllegalStateException(
                "Tour 장소 수집에 실패했습니다. target=" + target + ", page=" + pageNo + ", reason=" + message);
    }

    private Integer parseInteger(String value) {
        return isBlank(value) ? null : Integer.valueOf(value);
    }

    private String joinAddress(String address, String detailAddress) {
        return isBlank(detailAddress) ? address : address + " " + detailAddress;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record PrioritizedPlace(ExternalPlaceSyncData place, int categoryPriority) {
    }
}
