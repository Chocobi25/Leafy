package com.chocobi.leafy.place.fetcher.image;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.image.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ImageSearchClient {
    private final WebClient naverWebClient;

    public SearchResponse searchImage(String query) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return naverWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.SEARCH_IMAGE_PATH)
                        .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8))
                        .queryParam("display", 10)
                        .queryParam("start", 1)
                        .build()
                )
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .block();
    }
}
