package com.chocobi.leafy.place.service;

import com.chocobi.leafy.constants.PlaceConstants;
import com.chocobi.leafy.place.dto.rural.RuralApiResponse;
import com.chocobi.leafy.place.dto.rural.RuralItem;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuralService {
    private final WebClient cultureWebClient;
    private final PlaceRepository placeRepository;

    @Value("${rural.api.key}")
    private String serviceKey;

    public RuralApiResponse<RuralItem> searchRural() {
        return cultureWebClient.get()
                .uri(this::buildSearchRuralUri)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RuralApiResponse<RuralItem>>() {})
                .block();
    }

    private URI buildSearchRuralUri(UriBuilder builder) {
        return builder.path(PlaceConstants.RURAL_PATH)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", "1000")
                .queryParam("pageNo", "1")
                .queryParam("keyword", "")
                .queryParam("where", "")
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .build();
    }

    public void saveRuralPlace() {
        RuralApiResponse<RuralItem> ruralApiResponse = searchRural();

        List<Place> places = ruralApiResponse.getResponse().getBody().getItems().getItem().stream()
                .map(item -> {
                    double[] coords = parseSpatial(item.getSpatial());
                    return Place.builder()
                            .title(item.getTitle())
                            .description(item.getDescription())
                            .category(Category.EXPERIENCE)
                            .address(item.getAffiliation())
                            .longitude(coords[0])
                            .latitude(coords[1])
                            .tel(item.getReference())
                            .url(item.getSource())
                            .copyright(item.getRights())
                            .build();
                })
                .toList();

        placeRepository.saveAll(places);
    }

    private double[] parseSpatial(String spatial) {
        String[] parts = spatial.split(",");
        double[] result = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i]);
        }

        return result;
    }
}
