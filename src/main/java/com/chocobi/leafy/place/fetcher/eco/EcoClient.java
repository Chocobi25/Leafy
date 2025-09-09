package com.chocobi.leafy.place.fetcher.eco;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.eco.dto.EcoItem;
import com.chocobi.leafy.place.fetcher.eco.dto.EcoApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@RequiredArgsConstructor
public class EcoClient {
    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String tourApiKey;

    public EcoApiResponse<EcoItem> fetchEcoPlaces() {
        return tourWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.ECO_PATH)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                        .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                        .queryParam("MobileApp", PlaceConstants.APP_NAME)
                        .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                        .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EcoApiResponse<EcoItem>>() {})
                .block();
    }
}
