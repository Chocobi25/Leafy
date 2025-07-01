package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.PlaceConstants;
import com.chocobi.leafy.place.dto.area.AreaCodeItem;
import com.chocobi.leafy.place.dto.area.AreaCodeApiResponse;
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

    private AreaCodeApiResponse<AreaCodeItem> fetchAreaCodes() {
        return tourWebClient.get()
                .uri(this::buildAreaCodeUri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AreaCodeApiResponse<AreaCodeItem>>() {})
                .block();
    }

    private URI buildAreaCodeUri(UriBuilder builder) {
        return builder.path(PlaceConstants.AREA_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                .queryParam("MobileApp", PlaceConstants.APP_NAME)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }

    public void saveAreaCode() {
        AreaCodeApiResponse<AreaCodeItem> response = fetchAreaCodes();

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

    public List<AreaCodeItem> getAreaCode() {
        List<AreaCode> areaCodes = areaCodeRepository.findAll();
        return areaCodes.stream()
                .map(areaCode -> new AreaCodeItem(String.valueOf(areaCode.getCode()), areaCode.getName()))
                .collect(Collectors.toList());
    }
}