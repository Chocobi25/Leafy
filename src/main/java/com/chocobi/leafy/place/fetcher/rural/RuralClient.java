package com.chocobi.leafy.place.fetcher.rural;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.rural.dto.RuralApiResponse;
import com.chocobi.leafy.place.fetcher.rural.dto.RuralItem;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class RuralClient {
    private final WebClient ruralWebClient;

    public RuralApiResponse<RuralItem> fetchRuralPlaces() {
        return ruralWebClient.get()
                .uri(this::buildRuralUri)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RuralApiResponse<RuralItem>>() {})
                .block();
    }

    private URI buildRuralUri(UriBuilder builder) {
        return builder.path(PlaceConstants.RURAL_PATH)
                .queryParam("numOfRows",PlaceConstants.MAX_NUM_OF_ROWS)
                .queryParam("pageNo", PlaceConstants.PAGE_NO)
                .queryParam("keyword", PlaceConstants.BLANK)
                .queryParam("where", PlaceConstants.BLANK)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }
}
