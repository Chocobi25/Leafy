package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.CarbonEmissionConst;
import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class CarDistanceService {

    private final WebClient kakaoNaviWebClient;

    public CarDistanceService(WebClient kakaoNaviWebClient) {
        this.kakaoNaviWebClient = kakaoNaviWebClient;
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     * @param request
     * @return
     */
    public DistanceResponse getDistance(CarDistanceRequest request) {

        // uri 경로
        String uri = "/v1/waypoints/directions";

        Map<String, Object> body = kakaoNaviWebClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // 동기 호출

        if (body == null || !body.containsKey("routes")) {
            throw new RuntimeException("카카오 API 응답 오류");
        }

        // 거리, 시간 정보 추출
        List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
        Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary"); // routes[0]의 summary를 꺼내기

        // summary의 distance와 duration 꺼내기
        int distance = (int) summary.get("distance");
        int duration = (int) summary.get("duration");
        double carbonEmission = distance / 1000.0 * CarbonEmissionConst.CAR_EMISSION;

        return new DistanceResponse(distance, duration, carbonEmission);
    }
}
