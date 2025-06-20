package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.PlaceConstants;
import com.chocobi.leafy.place.dto.ThemeApiResponse;
import com.chocobi.leafy.place.dto.ThemeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final WebClient cultureWebClient;

    @Value("${theme.api.key}")
    private String serviceKey;

    public ThemeApiResponse<ThemeItem> searchTheme() {
        return cultureWebClient.get()
                .uri(this::buildSearchThemeUri)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ThemeApiResponse<ThemeItem>>() {})
                .block();
    }

    private URI buildSearchThemeUri(UriBuilder builder) {
        return builder.path(PlaceConstants.THEME_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", "1400")
                .queryParam("pageNo", "1")
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }

    public List<ThemeItem> filteredByArea(String area) {
        ThemeApiResponse<ThemeItem> response = searchTheme();

        return response.getResponse().getBody().getItems().getItem().stream()
                .filter(item -> item.getDescription() != null) // 장소 설명 없으면 제외.
                .filter(item -> item.getSpatial() != null && item.getSpatial().contains(area)) // 사용자가
                .collect(Collectors.toList());
    }
}
