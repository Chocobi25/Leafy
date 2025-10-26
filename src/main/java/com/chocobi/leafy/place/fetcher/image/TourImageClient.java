package com.chocobi.leafy.place.fetcher.image;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.image.dto.TourImageApiResponse;
import com.chocobi.leafy.place.fetcher.image.dto.TourImageItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class TourImageClient {
    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String tourApiKey;

    public Mono<TourImageApiResponse<TourImageItem>> fetchTourImages(String keyword) {
        return tourWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.TOUR_IMAGE_PATH)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                        .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                        .queryParam("MobileApp", PlaceConstants.APP_NAME)
                        .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                        .queryParam("keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8))
                        .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<TourImageApiResponse<TourImageItem>>() {});
    }
}
