package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.config.ODsayConfig;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class TransDistanceService {

    private final ODsayConfig odsayConfig;;
    private final RestTemplate restTemplate;

    public TransDistanceService(ODsayConfig odsayConfig) {
        this.odsayConfig = odsayConfig;
        this.restTemplate = new RestTemplate();
        System.out.println("odsay key = " + this.odsayConfig.getApiKey());
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return
     */
    public DistanceResponse getDistance(String fromX, String fromY, String toX, String toY) {
        String url = "https://api.odsay.com/v1/api/searchPubTransPathT";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("SX", fromX)
                .queryParam("SY", fromY)
                .queryParam("EX", toX)
                .queryParam("EY", toY)
                .queryParam("apiKey", odsayConfig.getApiKey());


        ResponseEntity<Map> response = restTemplate.getForEntity(builder.toUriString(), Map.class);
        Map<String, Object> body = response.getBody();
        if(body == null) {
            throw new RuntimeException("ODsay API 응답 오류");
        }

        System.out.println("body = " + body);

        List<Map<String, Object>> paths = (List<Map<String, Object>>) ((Map<String, Object>) body.get("result")).get("path");
        if (paths == null || paths.isEmpty()) {
            throw new RuntimeException("경로를 찾을 수 없습니다.");
        }

        Map<String, Object> firstPath = paths.get(0);
        Map<String, Object> info = (Map<String, Object>) firstPath.get("info");

        double totalDistance = (double) info.get("totalDistance");
        int totalTime = (int) info.get("totalTime");

        DistanceResponse result = new DistanceResponse(totalDistance, totalTime);
        return result;
    }
}
