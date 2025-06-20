package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.PlaceConstants;
import com.chocobi.leafy.place.dto.RuralApiResponse;
import com.chocobi.leafy.place.dto.RuralItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class RuralService {
    private final WebClient cultureWebClient;

    @Value("${rural.api.key}")
    private String serviceKey;

    public RuralApiResponse<RuralItem> searchRural() {
        return cultureWebClient.get()
                .uri(this::buildSearchRuralUri)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RuralApiResponse<RuralItem>>() {})
                .block();
    }

    private URI buildSearchRuralUri(UriBuilder builder) {
        return builder.path(PlaceConstants.RURAL_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", "10")
                .queryParam("pageNo", "1")
                .queryParam("keyword", "")
                .queryParam("where", "")
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }
}
