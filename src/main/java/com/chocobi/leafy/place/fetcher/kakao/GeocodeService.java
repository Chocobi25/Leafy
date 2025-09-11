package com.chocobi.leafy.place.fetcher.kakao;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.kakao.dto.Document;
import com.chocobi.leafy.place.fetcher.kakao.dto.GeocodeResponse;
import com.chocobi.leafy.place.fetcher.kakao.dto.GeocodeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class GeocodeService {
    private final WebClient kakaoWebClient;

    @Value("${kakao.api.key}")
    private String appKey;

    public GeocodeResult getCoordinatesFromAddress(String address) {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        GeocodeResponse response = requestGeocode(encodedAddress);
        return extractCoordinatesAndAddress(response);
    }

    private GeocodeResponse requestGeocode(String encodedAddress) {
        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PlaceConstants.GEOCODE_PATH)
                        .queryParam("query", encodedAddress)
                        .build(false))  // false로 설정해 인코딩 중복 방지
                .header("Authorization", "KakaoAK " + appKey)
                .retrieve()
                .bodyToMono(GeocodeResponse.class)
                .block();
    }

    private GeocodeResult extractCoordinatesAndAddress(GeocodeResponse response) {
        if (response == null || response.getDocuments().isEmpty()) {
            return new GeocodeResult(PlaceConstants.DEFAULT_COORDINATES[0], PlaceConstants.DEFAULT_COORDINATES[1], null);
        }

        Document doc = response.getDocuments().getFirst();
        try {
            double x = Double.parseDouble(doc.getX());
            double y = Double.parseDouble(doc.getY());
            String fullAddress = doc.getAddress_name();
            return new GeocodeResult(y, x, fullAddress);
        } catch (NumberFormatException e) {
            return new GeocodeResult(PlaceConstants.DEFAULT_COORDINATES[0], PlaceConstants.DEFAULT_COORDINATES[1], null);
        }
    }
}
