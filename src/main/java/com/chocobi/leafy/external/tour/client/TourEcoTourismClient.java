package com.chocobi.leafy.external.tour.client;

import com.chocobi.leafy.external.tour.dto.TourEcoTourismResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@RequiredArgsConstructor
public class TourEcoTourismClient {
    private static final String ecoPath = "/GreenTourService1/areaBasedList1";
    private static final int DEFAULT_NUM_OF_ROWS = 200;
    private static final String MOBILE_OS = "ETC";
    private static final String MOBILE_APP = "Leafy";
    private static final String RESPONSE_TYPE_JSON = "json";

    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String tourApiKey;

    public TourEcoTourismResponse fetchEcoTourismPlaces() {
        return tourWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ecoPath)
                        .queryParam("serviceKey", tourApiKey)
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("MobileOS", MOBILE_OS)
                        .queryParam("MobileApp", MOBILE_APP)
                        .queryParam("_type", RESPONSE_TYPE_JSON)
                        .build()
                )
                .retrieve()
                .bodyToMono(TourEcoTourismResponse.class)
                .block(Duration.ofSeconds(10));
    }
}
