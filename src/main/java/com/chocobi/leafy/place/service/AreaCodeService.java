package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.TourConstants;
import com.chocobi.leafy.place.dto.AreaCodeItem;
import com.chocobi.leafy.place.dto.AreaCodeResponse;
import com.chocobi.leafy.place.dto.PlaceItem;
import com.chocobi.leafy.place.dto.PlaceResponse;
import com.chocobi.leafy.place.entity.AreaCode;
import com.chocobi.leafy.place.repository.AreaCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaCodeService {
    private final WebClient tourWebClient;
    private final AreaCodeRepository areaCodeRepository;

    @Value("${tour.api.key}")
    private String serviceKey;

    @Value("${tour.api.area.url}")
    private String areaUrl;

    private AreaCodeResponse<AreaCodeItem> fetchAreaCodes() {
        return tourWebClient.get()
                .uri(areaUrl,
                        serviceKey, TourConstants.MOBILE_OS, TourConstants.APP_NAME, TourConstants.RESPONSE_TYPE_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AreaCodeResponse<AreaCodeItem>>() {})
                .block();
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
}