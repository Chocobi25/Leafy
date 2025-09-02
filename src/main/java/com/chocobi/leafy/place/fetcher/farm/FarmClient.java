package com.chocobi.leafy.place.fetcher.farm;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmDetailApiResponse;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmListApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@RequiredArgsConstructor
public class FarmClient {
    private final WebClient farmWebClient;

    @Value("${farm.api.key}")
    private String apiKey;

    // 목록 조회
    public FarmListApiResponse fetchFarmList() {
        return farmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.FARM_LIST)
                        .queryParam("apiKey", apiKey)
                        .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                        .queryParam("pageNo", PlaceConstants.PAGE_NO)
                        .build()
                )
                .retrieve()
                .bodyToMono(FarmListApiResponse.class)
                .block();
    }

    // 상세 조회
    public FarmDetailApiResponse fetchFarmDetail(String contentId) {
        return farmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.FARM_DETAIL)
                        .queryParam("apiKey", apiKey)
                        .queryParam("cntntsNo", contentId)
                        .build()
                )
                .retrieve()
                .bodyToMono(FarmDetailApiResponse.class)
                .block();
    }
}
