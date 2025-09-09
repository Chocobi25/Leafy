package com.chocobi.leafy.place.fetcher.theme;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.theme.dto.ThemeApiResponse;
import com.chocobi.leafy.place.fetcher.theme.dto.ThemeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ThemeClient {
    private final WebClient cultureWebClient;

    @Value("${theme.api.key}")
    private String serviceKey;

    public ThemeApiResponse<ThemeItem> fetchThemePlaces() {
        return cultureWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.THEME_PATH)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("numOfRows", PlaceConstants.MAX_NUM_OF_ROWS)
                        .queryParam("pageNo", PlaceConstants.PAGE_NO)
                        .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                        .build()
                )
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ThemeApiResponse<ThemeItem>>() {})
                .block();
    }
}
