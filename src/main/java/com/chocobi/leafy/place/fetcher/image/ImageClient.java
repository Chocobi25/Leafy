package com.chocobi.leafy.place.fetcher.image;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.image.dto.ImageItem;
import com.chocobi.leafy.place.fetcher.image.dto.ImageApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class ImageClient {
    private final WebClient tourWebClient;

    public ImageApiResponse<ImageItem> searchImage(String keyword) {
        return tourWebClient.get()
                .uri(uriBuilder -> buildImageUri(uriBuilder, keyword))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ImageApiResponse<ImageItem>>() {})
                .block();
    }

    private URI buildImageUri(UriBuilder builder, String keyword) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        return builder.path(PlaceConstants.IMAGE_PATH)
                .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                .queryParam("MobileOS", PlaceConstants.MOBILE_OS)
                .queryParam("MobileApp", PlaceConstants.APP_NAME)
                .queryParam("_type", PlaceConstants.RESPONSE_TYPE_JSON)
                .queryParam("keyword", encodedKeyword)
                .build();
    }
}