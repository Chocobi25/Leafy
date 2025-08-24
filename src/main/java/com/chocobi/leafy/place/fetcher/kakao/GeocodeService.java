package com.chocobi.leafy.place.fetcher.kakao;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.kakao.dto.Document;
import com.chocobi.leafy.place.fetcher.kakao.dto.GeocodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class GeocodeService {
    private final WebClient kakaoWebClient;

    public double[] getCoordinatesFromAddress(String address) {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        GeocodeResponse response = requestGeocode(encodedAddress);
        return extractCoordinates(response);
    }

    private GeocodeResponse requestGeocode(String encodedAddress) {
        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.GEOCODE_PATH)
                        .queryParam("query", encodedAddress)
                        .build(false))  // false로 설정해 인코딩 중복 방지
                .retrieve()
                .bodyToMono(GeocodeResponse.class)
                .block();
    }

    private double[] extractCoordinates(GeocodeResponse response) {
        if (response == null || response.getDocuments().isEmpty()) {
            return PlaceConstants.DEFAULT_COORDINATES;
        }

        Document doc = response.getDocuments().getFirst();
        try {
            double x = Double.parseDouble(doc.getX());
            double y = Double.parseDouble(doc.getY());
            return new double[]{x, y};
        } catch (NumberFormatException e) {
            return PlaceConstants.DEFAULT_COORDINATES;
        }
    }
}
