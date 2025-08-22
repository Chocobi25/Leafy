package com.chocobi.leafy.place.fetcher.tour;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.tour.dto.EcoItem;
import com.chocobi.leafy.place.fetcher.tour.dto.EcoApiResponse;
import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.repository.PlaceRepository;
import com.chocobi.leafy.place.fetcher.kakao.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EcoService {
    private final WebClient tourWebClient;
    private final PlaceRepository placeRepository;
    private final GeocodeService geocodeService;


    public EcoApiResponse<EcoItem> searchEcoPlace() {
        return tourWebClient.get()
                .uri(this::buildSearchEcoUri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EcoApiResponse<EcoItem>>() {})
                .block();
    }

    private URI buildSearchEcoUri(UriBuilder builder) {
        return builder.path(PlaceConstants.ECO_PATH)
                .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                .queryParam("MobileApp", PlaceConstants.APP_NAME)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }

    public void saveEcoPlace() {
        EcoApiResponse<EcoItem> ecoApiResponse = searchEcoPlace();
        List<Place> list = ecoApiResponse.getResponse().getBody().getItems().getItem().stream()
                .map(item -> {
                    double[] coords = geocodeService.getCoordinatesFromAddress(item.getAddr());

                    return Place.builder()
                            .title(item.getTitle())
                            .description(item.getSummary())
                            .category(Category.NATURE)
                            .address(item.getAddr())
                            .longitude(coords[0])
                            .latitude(coords[1])
                            .tel(item.getTel())
                            .imageUrl(item.getMainimage())
                            .copyright("한국관광공사")
                            .sourceType(PlaceSourceType.API)
                            .build();
                })
                .toList();
        placeRepository.saveAll(list);
    }
}
