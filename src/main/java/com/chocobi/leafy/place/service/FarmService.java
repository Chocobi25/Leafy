package com.chocobi.leafy.place.service;

import com.chocobi.leafy.place.dto.FarmApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final WebClient farmWebClient;

    @Value("${farm.api.key}")
    private String apiKey;

    public FarmApiResponse searchFarm(String path) {
        return farmWebClient.get()
                .uri(uriBuilder -> buildSearchUri(uriBuilder, path))
                .retrieve()
                .bodyToMono(FarmApiResponse.class)
                .block();

    }

    private URI buildSearchUri(UriBuilder builder, String path) {
        return builder.path(path)
                .queryParam("apiKey", apiKey)
                .queryParam("numOfRows", "200")
                .queryParam("pageNo", "1")
                .build();
    }


}
