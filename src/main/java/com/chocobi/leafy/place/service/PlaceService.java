package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.TourConstants;
import com.chocobi.leafy.place.dto.PlaceItem;
import com.chocobi.leafy.place.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
public class PlaceService {
    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String serviceKey;

    @Value("${tour.api.place.url}")
    private String placeUrl;


    public PlaceResponse<PlaceItem> searchPlace(int areaCode) {
        return tourWebClient.get()
                .uri(placeUrl,
                        serviceKey, areaCode, TourConstants.MOBILE_OS, TourConstants.APP_NAME, TourConstants.RESPONSE_TYPE_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PlaceResponse<PlaceItem>>() {})
                .block();
    }
}
