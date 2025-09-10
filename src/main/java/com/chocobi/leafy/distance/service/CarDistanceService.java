package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.DistanceConst;
import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.Point;
import com.chocobi.leafy.distance.dto.*;
import com.chocobi.leafy.util.CarbonCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarDistanceService {

    private final WebClient kakaoNaviWebClient;

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     */
    public CarDistanceResponse getDistance(CarDistanceRequest request) {
        KakaoNaviResponse kakaoNaviResponse = callKakaoApi(request);

        List<Routes> routesList = kakaoNaviResponse.getRoutes();
        if (routesList == null || routesList.isEmpty()) {
            throw new RuntimeException("카카오 API 응답에 경로(routes)가 없음");
        }

        Routes routes = routesList.getFirst();
        if (routes == null) {
            throw new RuntimeException("카카오 API 응답 오류");
        }

        // 에러 코드 체크
        if (routes.getResult_code() != null && routes.getResult_code() != 0) {
            int errorCode = routes.getResult_code();
            String errorMsg = routes.getResult_message();

            // waypoints가 있는 경우 구간별 계산으로 fallback
            if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
                return getDistanceBySegments(request);
            }

            // waypoints가 없는 단순 경로도 실패한 경우
            throw new RuntimeException("카카오 네비 API 에러 (코드: " + errorCode + ", 메시지: " + errorMsg + ")");
        }

        // summary 꺼내기
        Summary summary = routes.getSummary();
        if (summary == null) {
            throw new RuntimeException("카카오 API summary 정보 없음");
        }

        // summary의 distance와 duration 꺼내기
        int distance = summary.getDistance();
        int duration = summary.getDuration();
        double carbonEmission = CarbonCalculator.CalculateCarCarbonEmission(distance);


        // 카카오에서 제공하는 section 구간별 처리
        List<Section> sections = routes.getSections();
        for (Section section : sections) {
            double sectionCarbonEmission = CarbonCalculator.CalculateCarCarbonEmission(section.getDistance());
            section.setCarbonEmission(sectionCarbonEmission);
            section.setMaxCarbonEmission(sectionCarbonEmission); // 자동차는 단일 교통수단
        }

        CarDistanceResponse carDistanceResponse = new CarDistanceResponse();
        carDistanceResponse.setDistanceResponse(new DistanceResponse(distance, duration, carbonEmission));
        carDistanceResponse.setSections(sections);

        return carDistanceResponse;
    }

    /**
     * 카카오 내비 API 호출
     */
    private KakaoNaviResponse callKakaoApi(CarDistanceRequest request) {
        KakaoNaviResponse response = kakaoNaviWebClient.post()
                .uri(DistanceConst.kakaoUri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(KakaoNaviResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("카카오 네비 API 응답 실패 (null)");
        }
        return response;
    }


    /**
     * api 호출 에러 시, 구간별 거리, 시간 계산
     */
    private CarDistanceResponse getDistanceBySegments(CarDistanceRequest request) {
        // 전체 경로 점들 (origin + waypoints + destination)
        List<Point> allPoints = DistanceUtils.buildAllPoints(request.getOrigin(), request.getDestination(), request.getWaypoints());

        CarDistanceResponse carDistanceResponse = new CarDistanceResponse();
        List<Section> sections = new ArrayList<>();

        double totalDistance = 0;
        double totalDuration = 0;

        // 각 구간별로 계산
        for (int i = 0; i < allPoints.size() - 1; i++) {
            Point start = allPoints.get(i);
            Point end = allPoints.get(i + 1);

            try {
                CarDistanceRequest segmentRequest = new CarDistanceRequest();
                segmentRequest.setOrigin(start);
                segmentRequest.setDestination(end);
                segmentRequest.setWaypoints(null); // waypoints 제거하여 단순 경로로

                CarDistanceResponse segmentResponse = getDistance(segmentRequest);
                totalDistance += segmentResponse.getDistanceResponse().getDistance();
                totalDuration += segmentResponse.getDistanceResponse().getDuration();
                
                // 구간별 Section 생성
                Section segmentSection = new Section();
                segmentSection.setDistance((int) segmentResponse.getDistanceResponse().getDistance());
                segmentSection.setDuration(segmentResponse.getDistanceResponse().getDuration());
                segmentSection.setCarbonEmission(segmentResponse.getDistanceResponse().getCarbonEmission());
                segmentSection.setMaxCarbonEmission(segmentResponse.getDistanceResponse().getCarbonEmission());
                sections.add(segmentSection);

            } catch (Exception e) {
                // 직선 거리 계산(하버사인 공식)
                double straightDistance = DistanceUtils.calculateStraightDistance(start, end);
                double estimatedDistance = straightDistance * DistanceConst.ROAD_CORRECTION_FACTOR; // 도로 보정계수
                double estimatedDuration = estimatedDistance / DistanceConst.AVERAGE_CAR_SPEED_MPS; // 평균 50km/h 가정, 초 단위

                totalDistance += estimatedDistance;
                totalDuration += estimatedDuration;
                
                // 추정값으로 Section 생성
                Section estimatedSection = new Section();
                estimatedSection.setDistance((int) estimatedDistance);
                estimatedSection.setDuration((int) estimatedDuration);
                double estimatedCarbonEmission = CarbonCalculator.CalculateCarCarbonEmission(estimatedDistance);
                estimatedSection.setCarbonEmission(estimatedCarbonEmission);
                estimatedSection.setMaxCarbonEmission(estimatedCarbonEmission);
                sections.add(estimatedSection);
            }
        }

        double carbonEmission = CarbonCalculator.CalculateCarCarbonEmission(totalDistance);
        carDistanceResponse.setDistanceResponse(new DistanceResponse(totalDistance, (int) totalDuration, carbonEmission));
        carDistanceResponse.setSections(sections);

        return carDistanceResponse;
    }
}