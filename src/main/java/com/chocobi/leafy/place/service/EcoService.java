package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.PlaceConstants;
import com.chocobi.leafy.place.dto.EcoItem;
import com.chocobi.leafy.place.dto.EcoApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;


@Service
@RequiredArgsConstructor
public class EcoService {
    private final WebClient tourWebClient;

    @Value("${tour.api.key}")
    private String serviceKey;

    public EcoApiResponse<EcoItem> searchEcoPlace(int areaCode) {
        return tourWebClient.get()
                .uri(uriBuilder -> buildSearchEcoUri(uriBuilder, areaCode))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EcoApiResponse<EcoItem>>() {})
                .block();
    }

    private URI buildSearchEcoUri(UriBuilder builder, int areaCode) {
        return builder.path(PlaceConstants.ECO_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", "100")
                .queryParam("areaCode", areaCode)
                .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                .queryParam("MobileApp", PlaceConstants.APP_NAME)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }
}
