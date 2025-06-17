package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.config.KakaoConfig;
import com.chocobi.leafy.distance.domain.CarDistanceResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class CarDistanceService {

    private final KakaoConfig kakaoConfig; // API 키 보관
    private final RestTemplate restTemplate; // HTTP 요청을 보내는 데 사용하는 Spring 제공 객체

    public CarDistanceService(KakaoConfig kakaoConfig) {
        this.kakaoConfig = kakaoConfig;
        this.restTemplate = new RestTemplate();
        System.out.println("kakao api key = " + this.kakaoConfig.getApiKey());
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     * @param from
     * @param to
     * @return
     */
    public CarDistanceResponse getDistance(String from, String to) {

        System.out.println("kakao api key = " + this.kakaoConfig.getApiKey());
        String url = "https://apis-navi.kakaomobility.com/v1/directions";
        HttpHeaders headers = new HttpHeaders(); // Header 생성
        headers.set("Authorization", "KakaoAK " + kakaoConfig.getApiKey()); // Header에 Authorization: KakaoAK <API_KEY> 설정

        // 파라미터 설정(GET 쿼리)
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url) // URI에 쿼리 파라미터로 origin, destination 추가
                .queryParam("origin", from)
                .queryParam("destination", to);

        // 요청 전송 및 응답 수신
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, Map.class // GET 요청 전송, 응답은 JSON이기 때문에 Map 타입으로 파싱
        );

        // 응답 파싱
        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("routes")) { // 응답 body에서 routes 키가 있는지 확인(없으면 예외 발생)
            throw new RuntimeException("카카오 API 응답 오류");
        }

        // 거리, 시간 정보 추출
        List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
        Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary"); // routes[0]의 summary를 꺼내기

        // summary의 distance와 duration 꺼내기
        int distance = (int) summary.get("distance");
        int duration = (int) summary.get("duration");

        return new CarDistanceResponse(distance, duration);
    }
}
