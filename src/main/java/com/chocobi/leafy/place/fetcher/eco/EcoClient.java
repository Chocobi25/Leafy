package com.chocobi.leafy.place.fetcher.eco;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.eco.dto.EcoItem;
import com.chocobi.leafy.place.fetcher.eco.dto.EcoApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class EcoClient {
    private final WebClient tourWebClient;

    public EcoApiResponse<EcoItem> fetchEcoPlaces() {
        return tourWebClient.get()
                .uri(this::buildEcoUri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EcoApiResponse<EcoItem>>() {})
                .block();
    }

    private URI buildEcoUri(UriBuilder builder) {
        return builder.path(PlaceConstants.ECO_PATH)
                .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                .queryParam("MobileApp", PlaceConstants.APP_NAME)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }
}
