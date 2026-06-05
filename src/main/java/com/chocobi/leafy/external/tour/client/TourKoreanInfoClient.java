package com.chocobi.leafy.external.tour.client;

import com.chocobi.leafy.external.tour.dto.TourKoreanAreaBasedResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanDetailImageResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanPlaceDetailResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanPlaceSearchCondition;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Component
@RequiredArgsConstructor
public class TourKoreanInfoClient {
    private static final String AREA_BASED_PATH = "/KorService2/areaBasedList2";
    private static final String PLACE_DETAIL_PATH = "/KorService2/detailCommon2";
    private static final String DETAIL_IMAGE_PATH = "/KorService2/detailImage2";
    private static final int DEFAULT_NUM_OF_ROWS = 100;
    private static final int DEFAULT_PAGE_NO = 1;
    private static final String MOBILE_OS = "ETC";
    private static final String MOBILE_APP = "Leafy";
    private static final String RESPONSE_TYPE_JSON = "json";
    private static final String ARRANGE_MODIFIED_DESC = "Q";
    private static final String IMAGE_YN = "Y";
    private static final String SUB_IMAGE_YN = "Y";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String tourApiKey;

    public TourKoreanAreaBasedResponse fetchAreaBasedPlaces(
            TourKoreanPlaceSearchCondition condition,
            int pageNo,
            int numOfRows
    ) {
        return tourWebClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = baseListUri(uriBuilder, AREA_BASED_PATH, pageNo, numOfRows)
                            .queryParam("arrange", ARRANGE_MODIFIED_DESC);
                    addPlaceSearchCondition(builder, condition);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(TourKoreanAreaBasedResponse.class)
                .block(REQUEST_TIMEOUT);
    }

    public TourKoreanPlaceDetailResponse fetchPlaceDetail(String contentId, Integer contentTypeId) {
        return tourWebClient.get()
                .uri(uriBuilder -> baseUri(uriBuilder, PLACE_DETAIL_PATH)
                        .queryParam("contentId", contentId)
                        .queryParamIfPresent("contentTypeId", java.util.Optional.ofNullable(contentTypeId))
                        .queryParam("defaultYN", "Y")
                        .queryParam("firstImageYN", "Y")
                        .queryParam("areacodeYN", "Y")
                        .queryParam("catcodeYN", "Y")
                        .queryParam("addrinfoYN", "Y")
                        .queryParam("mapinfoYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .build())
                .retrieve()
                .bodyToMono(TourKoreanPlaceDetailResponse.class)
                .block(REQUEST_TIMEOUT);
    }

    public TourKoreanDetailImageResponse fetchDetailImages(String contentId) {
        return tourWebClient.get()
                .uri(uriBuilder -> baseListUri(uriBuilder, DETAIL_IMAGE_PATH, DEFAULT_PAGE_NO, DEFAULT_NUM_OF_ROWS)
                        .queryParam("contentId", contentId)
                        .queryParam("imageYN", IMAGE_YN)
                        .queryParam("subImageYN", SUB_IMAGE_YN)
                        .build())
                .retrieve()
                .bodyToMono(TourKoreanDetailImageResponse.class)
                .block(REQUEST_TIMEOUT);
    }

    private UriBuilder baseListUri(UriBuilder uriBuilder, String path, int pageNo, int numOfRows) {
        return baseUri(uriBuilder, path)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows);
    }

    private UriBuilder baseUri(UriBuilder uriBuilder, String path) {
        return uriBuilder
                .path(path)
                .queryParam("serviceKey", tourApiKey)
                .queryParam("MobileOS", MOBILE_OS)
                .queryParam("MobileApp", MOBILE_APP)
                .queryParam("_type", RESPONSE_TYPE_JSON);
    }

    private void addPlaceSearchCondition(UriBuilder builder, TourKoreanPlaceSearchCondition condition) {
        if (condition == null) {
            return;
        }

        addOptionalQueryParam(builder, "contentTypeId", condition.contentTypeId());
        addOptionalQueryParam(builder, "areaCode", condition.areaCode());
        addOptionalQueryParam(builder, "sigunguCode", condition.sigunguCode());
        addOptionalQueryParam(builder, "lclsSystm1", condition.lclsSystm1());
        addOptionalQueryParam(builder, "lclsSystm2", condition.lclsSystm2());
        addOptionalQueryParam(builder, "lclsSystm3", condition.lclsSystm3());
    }

    private void addOptionalQueryParam(UriBuilder builder, String name, Object value) {
        if (Objects.isNull(value)) {
            return;
        }

        if (value instanceof String stringValue && stringValue.isBlank()) {
            return;
        }

        builder.queryParam(name, value);
    }
}
