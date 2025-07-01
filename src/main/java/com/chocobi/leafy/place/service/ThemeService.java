package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.PlaceConstants;
import com.chocobi.leafy.place.dto.theme.ThemeApiResponse;
import com.chocobi.leafy.place.dto.theme.ThemeItem;
import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.repository.PlaceRepository;
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
public class ThemeService {
    private final WebClient cultureWebClient;
    private final GeocodeService geocodeService;
    private final PlaceRepository placeRepository;

    @Value("${theme.api.key}")
    private String serviceKey;

    public ThemeApiResponse<ThemeItem> searchTheme() {
        return cultureWebClient.get()
                .uri(this::buildSearchThemeUri)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ThemeApiResponse<ThemeItem>>() {})
                .block();
    }

    private URI buildSearchThemeUri(UriBuilder builder) {
        return builder.path(PlaceConstants.THEME_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", PlaceConstants.MAX_NUM_OF_ROWS)
                .queryParam("pageNo", PlaceConstants.PAGE_NO)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }

    public void saveThemePlace(){
        ThemeApiResponse<ThemeItem> response = searchTheme();

        List<Place> places = response.getResponse().getBody().getItems().getItem().stream()
                .filter(item -> item.getTitle() != null && item.getDescription() != null && item.getSpatial() != null)
                .map(item -> {
                    double[] coords = geocodeService.getCoordinatesFromAddress(item.getSpatial());

                    return Place.builder()
                            .title(item.getTitle())
                            .description(item.getDescription())
                            .tel(item.getReference())
                            .category(Category.CULTURE)
                            .copyright(item.getCreator())
                            .address(item.getSpatial())
                            .longitude(coords[0])
                            .latitude(coords[1])
                            .build();
                })
                .toList();

        placeRepository.saveAll(places);
    }
}
