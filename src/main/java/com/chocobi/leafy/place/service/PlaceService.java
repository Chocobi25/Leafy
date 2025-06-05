package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.TourConstants;
import com.chocobi.leafy.place.dto.PlaceItem;
import com.chocobi.leafy.place.dto.PlaceApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;


@Service
@RequiredArgsConstructor
public class PlaceService {
    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String serviceKey;

    public PlaceApiResponse<PlaceItem> searchPlace(int areaCode) {
        return tourWebClient.get()
                .uri(uriBuilder -> buildSearchPlaceUri(uriBuilder, areaCode))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PlaceApiResponse<PlaceItem>>() {})
                .block();
    }

    private URI buildSearchPlaceUri(UriBuilder builder, int areaCode) {
        return builder.path(TourConstants.PLACE_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", "100")
                .queryParam("areaCode", areaCode)
                .queryParam("MobileOS", TourConstants.MOBILE_OS)
                .queryParam("MobileApp", TourConstants.APP_NAME)
                .queryParam("_type", TourConstants.RESPONSE_TYPE_JSON)
                .build();
    }
}
