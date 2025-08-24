package com.chocobi.leafy.place.fetcher.theme;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.theme.dto.ThemeApiResponse;
import com.chocobi.leafy.place.fetcher.theme.dto.ThemeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class ThemeClient {
    private final WebClient themeWebClient;

    public ThemeApiResponse<ThemeItem> fetchThemePlaces() {
        return themeWebClient.get()
                .uri(this::buildThemeUri)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ThemeApiResponse<ThemeItem>>() {})
                .block();
    }

    private URI buildThemeUri(UriBuilder builder) {
        return builder.path(PlaceConstants.THEME_PATH)
                .queryParam("numOfRows", PlaceConstants.MAX_NUM_OF_ROWS)
                .queryParam("pageNo", PlaceConstants.PAGE_NO)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }
}
