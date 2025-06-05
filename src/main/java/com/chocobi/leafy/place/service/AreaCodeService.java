package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.TourConstants;
import com.chocobi.leafy.place.dto.AreaCodeItem;
import com.chocobi.leafy.place.dto.AreaCodeResponse;
import com.chocobi.leafy.place.dto.AreaApiResponse;
import com.chocobi.leafy.place.entity.AreaCode;
import com.chocobi.leafy.place.repository.AreaCodeRepository;
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
public class AreaCodeService {
    private final WebClient tourWebClient;
    private final AreaCodeRepository areaCodeRepository;

    @Value("${tour.api.key}")
    private String serviceKey;

    private AreaCodeResponse<AreaCodeItem> fetchAreaCodes() {
        return tourWebClient.get()
                .uri(this::buildAreaCodeUri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AreaCodeResponse<AreaCodeItem>>() {})
                .block();
    }

    private URI buildAreaCodeUri(UriBuilder builder) {
        return builder.path(TourConstants.AREA_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", TourConstants.MOBILE_OS)
                .queryParam("MobileApp", TourConstants.APP_NAME)
                .queryParam("_type", TourConstants.RESPONSE_TYPE_JSON)
                .build();
    }

    public void saveAreaCode() {
        AreaCodeResponse<AreaCodeItem> response = fetchAreaCodes();

        if (response != null && response.getResponse().getBody() != null && response.getResponse().getBody().getItems() != null) {
            List<AreaCodeItem> items = response.getResponse().getBody().getItems().getItem();

            List<AreaCode> areaCodes = items.stream()
                    .map(item -> {
                        int code = Integer.parseInt(item.getCode());
                        String name = item.getName();
                        return new AreaCode(code, name);
                    })
                    .toList();

            areaCodeRepository.saveAll(areaCodes);
        }
    }

    public List<AreaApiResponse> getAreaCode() {
        List<AreaCode> areaCodes = areaCodeRepository.findAll();
        return areaCodes.stream()
                .map(areaCode -> new AreaApiResponse(areaCode.getCode(), areaCode.getName()))
                .collect(Collectors.toList());
    }
}