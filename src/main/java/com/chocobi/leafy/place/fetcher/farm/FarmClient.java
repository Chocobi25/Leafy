package com.chocobi.leafy.place.fetcher.farm;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmDetailApiResponse;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmListApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class FarmClient {
    private final WebClient farmWebClient;

    // 목록 조회
    public FarmListApiResponse fetchFarmList() {
        return farmWebClient.get()
                .uri(this::buildFarmListUri)
                .retrieve()
                .bodyToMono(FarmListApiResponse.class)
                .block();
    }

    // 상세 조회
    public FarmDetailApiResponse fetchFarmDetail(String contentId) {
        return farmWebClient.get()
                .uri(uriBuilder -> buildFarmDetailUri(uriBuilder, contentId))
                .retrieve()
                .bodyToMono(FarmDetailApiResponse.class)
                .block();
    }


    private URI buildFarmListUri(UriBuilder builder) {
        return builder.path(PlaceConstants.FARM_LIST)
                .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                .queryParam("pageNo", PlaceConstants.PAGE_NO)
                .build();
    }

    private URI buildFarmDetailUri(UriBuilder builder, String contentId) {
        return builder.path(PlaceConstants.FARM_DETAIL)
                .queryParam("cntntsNo", contentId)
                .build();
    }
}
