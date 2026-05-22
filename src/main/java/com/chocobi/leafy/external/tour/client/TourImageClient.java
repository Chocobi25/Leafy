package com.chocobi.leafy.external.tour.client;

import com.chocobi.leafy.external.tour.dto.TourImageResponse.TourImageItem;
import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.external.tour.dto.TourImageResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class TourImageClient {
    private static final String imagePath = "/PhotoGalleryService1/gallerySearchList1";
    private static final int DEFAULT_NUM_OF_ROWS = 200;
    private static final String MOBILE_OS = "ETC";
    private static final String MOBILE_APP = "Leafy";
    private static final String RESPONSE_TYPE_JSON = "json";

    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String tourApiKey;

    public TourImageResponse fetchTourImages(String keyword) {
        return tourWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(imagePath)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("MobileOS", MOBILE_OS)
                        .queryParam("MobileApp", MOBILE_APP)
                        .queryParam("_type", RESPONSE_TYPE_JSON)
                        .queryParam("keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8))
                        .build()
                )
                .retrieve()
                .bodyToMono(TourImageResponse.class)
                .block(Duration.ofSeconds(10));
    }
}
