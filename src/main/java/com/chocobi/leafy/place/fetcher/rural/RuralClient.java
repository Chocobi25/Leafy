package com.chocobi.leafy.place.fetcher.rural;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.rural.dto.RuralApiResponse;
import com.chocobi.leafy.place.fetcher.rural.dto.RuralItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@RequiredArgsConstructor
public class RuralClient {
    private final WebClient cultureWebClient;

    @Value("${rural.api.key}")
    private String serviceKey;

    public RuralApiResponse<RuralItem> fetchRuralPlaces() {
        return cultureWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.RURAL_PATH)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("numOfRows", PlaceConstants.MAX_NUM_OF_ROWS)
                        .queryParam("pageNo", PlaceConstants.PAGE_NO)
                        .queryParam("keyword", PlaceConstants.BLANK)
                        .queryParam("where", PlaceConstants.BLANK)
                        .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                        .build()
                )
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RuralApiResponse<RuralItem>>() {})
                .block();
    }
}
