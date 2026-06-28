package com.chocobi.leafy.place.application;

import com.chocobi.leafy.external.common.dto.ExternalApiResponse;
import com.chocobi.leafy.external.farm.client.FarmRestaurantClient;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantDetailResponse;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantDetailResponse.FarmRestaurantDetailItem;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantListResponse;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantListResponse.FarmRestaurantListItem;
import com.chocobi.leafy.external.kakao.client.GeocodeClient;
import com.chocobi.leafy.external.kakao.dto.GeocodedAddress;
import com.chocobi.leafy.place.common.dto.ExternalPlaceSyncData;
import com.chocobi.leafy.place.batch.ExternalPlaceSyncService;
import com.chocobi.leafy.place.infra.entity.Category;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmPlaceSynchronizer implements ExternalPlaceSynchronizer {
    private static final int PAGE_SIZE = 100;
    private static final int SYNC_BATCH_SIZE = 100;
    private static final Set<String> SUCCESS_RESULT_CODES = Set.of("00", "0000");

    private final FarmRestaurantClient farmRestaurantClient;
    private final GeocodeClient geocodeClient;
    private final ExternalPlaceSyncService externalPlaceSyncService;

    @Override
    public int sync() {
        FarmCollectionResult collectionResult = collectPlaces();
        syncCollectedPlaces(collectionResult.collectedPlaces());
        int deactivatedCount = externalPlaceSyncService.deactivateMissing(
                ExternalPlaceSource.FARM_API,
                collectionResult.observedContentIds());
        log.info("Farm place sync completed. collected={}, deactivated={}",
                collectionResult.collectedPlaces().size(), deactivatedCount);
        return collectionResult.collectedPlaces().size();
    }

    private FarmCollectionResult collectPlaces() {
        Map<String, ExternalPlaceSyncData> collectedPlaces = new HashMap<>();
        Set<String> observedContentIds = new HashSet<>();
        int pageNo = 1;
        int fetchedCount = 0;
        int totalCount = Integer.MAX_VALUE;

        while (fetchedCount < totalCount) {
            FarmRestaurantListResponse response = farmRestaurantClient.fetchFarmList(pageNo, PAGE_SIZE);
            FarmRestaurantListResponse.Items items = validateListResponse(response, pageNo);
            validatePageMetadata(items, pageNo);
            totalCount = items.getTotalCount();

            if (pageNo == 1 && totalCount == 0) {
                throw new IllegalStateException("농가맛집 수집 결과가 없습니다.");
            }

            List<FarmRestaurantListItem> pageItems = getItems(items);
            if (pageItems.isEmpty() && fetchedCount < totalCount) {
                throw invalidResponse(pageNo, "empty page before totalCount");
            }
            pageItems.stream()
                    .map(FarmRestaurantListItem::getCntntsNo)
                    .filter(contentId -> !isBlank(contentId))
                    .forEach(observedContentIds::add);
            pageItems.stream()
                    .map(this::mapPlace)
                    .filter(Objects::nonNull)
                    .forEach(place -> collectedPlaces.put(place.contentId(), place));

            fetchedCount += pageItems.size();
            pageNo = items.getPageNo() + 1;
        }
        return new FarmCollectionResult(collectedPlaces, observedContentIds);
    }

    private ExternalPlaceSyncData mapPlace(FarmRestaurantListItem listItem) {
        if (isBlank(listItem.getCntntsNo())) {
            log.warn("Skipping farm place without content id");
            return null;
        }

        FarmRestaurantDetailResponse response = farmRestaurantClient.fetchFarmDetail(listItem.getCntntsNo());
        FarmRestaurantDetailItem detail = validateDetailResponse(response, listItem.getCntntsNo());
        if (detail == null || isBlank(detail.getCntntsSj()) || isBlank(detail.getLocplc())) {
            log.warn("Skipping invalid farm place. contentId={}", listItem.getCntntsNo());
            return null;
        }

        GeocodedAddress geocodedAddress = geocodeClient.getCoordinatesFromAddress(detail.getLocplc());
        if (geocodedAddress.getAddress() == null) {
            log.warn("Skipping farm place without coordinates. contentId={}", listItem.getCntntsNo());
            return null;
        }

        String address = geocodedAddress.getAddress().getAddress_name();
        return new ExternalPlaceSyncData(
                ExternalPlaceSource.FARM_API,
                Category.FOOD.name(),
                listItem.getCntntsNo(),
                null,
                detail.getCntntsSj(),
                address,
                geocodedAddress.getLatitude(),
                geocodedAddress.getLongitude(),
                "농촌진흥청",
                detail.getSmm(),
                detail.getTelno(),
                detail.getUrl(),
                null,
                null,
                null,
                createVersion(detail, address, geocodedAddress)
        );
    }

    private FarmRestaurantListResponse.Items validateListResponse(
            FarmRestaurantListResponse response,
            int pageNo
    ) {
        if (response == null || !isSuccessful(response.getHeader())) {
            throw invalidResponse(pageNo, responseMessage(response == null ? null : response.getHeader()));
        }
        if (response.getBody() == null || response.getBody().getItems() == null) {
            throw invalidResponse(pageNo, "missing body or items");
        }
        return response.getBody().getItems();
    }

    private FarmRestaurantDetailItem validateDetailResponse(
            FarmRestaurantDetailResponse response,
            String contentId
    ) {
        if (response == null || !isSuccessful(response.getHeader())) {
            throw new IllegalStateException("농가맛집 상세 수집에 실패했습니다. contentId=" + contentId
                    + ", reason=" + responseMessage(response == null ? null : response.getHeader()));
        }
        return response.getBody() == null ? null : response.getBody().getItem();
    }

    private boolean isSuccessful(ExternalApiResponse.Header header) {
        return header != null && SUCCESS_RESULT_CODES.contains(header.getResultCode());
    }

    private String responseMessage(ExternalApiResponse.Header header) {
        return header == null ? "missing header" : header.getResultCode() + ":" + header.getResultMsg();
    }

    private void validatePageMetadata(FarmRestaurantListResponse.Items items, int requestedPageNo) {
        if (items.getPageNo() != requestedPageNo || items.getNumOfRows() <= 0) {
            throw invalidResponse(requestedPageNo,
                    "invalid page metadata: pageNo=" + items.getPageNo() + ", numOfRows=" + items.getNumOfRows());
        }
    }

    private List<FarmRestaurantListItem> getItems(FarmRestaurantListResponse.Items items) {
        return items.getItem() == null ? List.of() : items.getItem();
    }

    private void syncCollectedPlaces(Map<String, ExternalPlaceSyncData> collectedPlaces) {
        List<ExternalPlaceSyncData> places = new ArrayList<>(collectedPlaces.values());
        for (int start = 0; start < places.size(); start += SYNC_BATCH_SIZE) {
            int end = Math.min(start + SYNC_BATCH_SIZE, places.size());
            externalPlaceSyncService.syncBatch(places.subList(start, end));
        }
    }

    private String createVersion(
            FarmRestaurantDetailItem detail,
            String address,
            GeocodedAddress geocodedAddress
    ) {
        String value = String.join("|",
                safe(detail.getCntntsSj()),
                safe(address),
                safe(detail.getTelno()),
                safe(detail.getUrl()),
                safe(detail.getSmm()),
                Double.toString(geocodedAddress.getLatitude()),
                Double.toString(geocodedAddress.getLongitude()));
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 해시를 사용할 수 없습니다.", exception);
        }
    }

    private IllegalStateException invalidResponse(int pageNo, String message) {
        return new IllegalStateException(
                "농가맛집 목록 수집에 실패했습니다. page=" + pageNo + ", reason=" + message);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record FarmCollectionResult(
            Map<String, ExternalPlaceSyncData> collectedPlaces,
            Set<String> observedContentIds
    ) {
    }
}
