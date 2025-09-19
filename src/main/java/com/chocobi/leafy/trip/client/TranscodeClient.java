package com.chocobi.leafy.trip.client;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TranscodeClient {
    private final WebClient kakaoWebClient;

    @Value("${kakao.api.key}")
    private String appKey;

    public TransCoordResponse requestGeocode(TransCoordDTO transCoordDTO) {
        System.out.println("좌표 API 호출: x=" + transCoordDTO.getX() + ", y=" + transCoordDTO.getY());

        TransCoordResponse response = kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/geo/coord2address.json")
                        .queryParam("x", transCoordDTO.getX())
                        .queryParam("y", transCoordDTO.getY())
                        .queryParam("input_coord", "WGS84")
                        .build(false))
                .header("Authorization", "KakaoAK " + appKey)
                .retrieve()
                .bodyToMono(TransCoordResponse.class)
                .block();

        System.out.println("카카오 API 응답: " + response);

        if (response != null && !response.getDocuments().isEmpty()) {
            System.out.println("첫 번째 주소: " + response.getDocuments().get(0).getAddress());
        } else {
            System.out.println("응답이 없거나 Documents가 비어있습니다.");
        }

        return response;
    }
}
