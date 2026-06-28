package com.chocobi.leafy.external.farm.client;

import com.chocobi.leafy.external.farm.dto.FarmRestaurantDetailResponse;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantListResponse;
import com.chocobi.leafy.place.common.util.PlaceConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;


@Component
@RequiredArgsConstructor
public class FarmRestaurantClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient farmWebClient;

    @Value("${farm.api.key}")
    private String apiKey;

    // 목록 조회
    public FarmRestaurantListResponse fetchFarmList(int pageNo, int numOfRows) {
        return farmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.FARM_LIST)
                        .queryParam("apiKey", apiKey)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("pageNo", pageNo)
                        .build()
                )
                .retrieve()
                .bodyToMono(FarmRestaurantListResponse.class)
                .block(REQUEST_TIMEOUT);
    }

    // 상세 조회
    public FarmRestaurantDetailResponse fetchFarmDetail(String contentId) {
        return farmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.FARM_DETAIL)
                        .queryParam("apiKey", apiKey)
                        .queryParam("cntntsNo", contentId)
                        .build()
                )
                .retrieve()
                .bodyToMono(FarmRestaurantDetailResponse.class)
                .block(REQUEST_TIMEOUT);
    }
}
