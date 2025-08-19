package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.CarbonEmissionConst;
import com.chocobi.leafy.constants.DistanceConst;
import com.chocobi.leafy.constants.Kakao;
import com.chocobi.leafy.constants.Transport;
import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.Point;
import com.chocobi.leafy.distance.dto.KakaoNaviResponse;
import com.chocobi.leafy.distance.dto.Routes;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.distance.dto.Summary;
import com.chocobi.leafy.trip.service.TripSegmentService;
import com.chocobi.leafy.util.CarbonCalculator;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CarDistanceService {

    private final WebClient kakaoNaviWebClient;
    private final TripSegmentService tripSegmentService;

    public CarDistanceService(WebClient kakaoNaviWebClient, TripSegmentService tripSegmentService) {
        this.kakaoNaviWebClient = kakaoNaviWebClient;
        this.tripSegmentService = tripSegmentService;
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     * @param request
     * @return
     */
    public DistanceResponse getDistance(CarDistanceRequest request) {

        KakaoNaviResponse kakaoNaviResponse = kakaoNaviWebClient.post()
                .uri(DistanceConst.kakaoUri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(KakaoNaviResponse.class)
                .block(); // 동기 호출

        List<Routes> routesList = kakaoNaviResponse.getRoutes();
        Routes routes = routesList.getFirst();

        if (routes == null) {
            throw new RuntimeException("카카오 API 응답 오류");
        }

        System.out.println("routes = " + routes);

        // 에러 코드 체크
        if (routes.getResult_code() != null && routes.getResult_code() != 0) {
            int errorCode = routes.getResult_code();
            String errorMsg = routes.getResult_message();
            System.out.println("전체 경로 실패 - 코드: " + errorCode + ", 메시지: " + errorMsg);

            // waypoints가 있는 경우 구간별 계산으로 fallback
            if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
                System.out.println("구간별 계산으로 fallback 시도");
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

        // section 꺼내기
        List<Section> sections = routes.getSections();
        for (Section section : sections) {
            double sectionCarbonEmission = CarbonCalculator.CalculateCarCarbonEmission(section.getDistance());
            section.setCarbonEmission(sectionCarbonEmission);
        }

        tripSegmentService.completeTempTripSegments(request.getTripId(), sections, Transport.CAR);

        return new DistanceResponse(distance, duration, carbonEmission);
    }

    /**
     * 출발지, 목적지와 웨이포인트를 리스트에 담기
     * @param request
     */
    private static List<Point> buildAllPoints(CarDistanceRequest request) {
        // 전체 경로 점들 (origin + waypoints + destination)
        List<Point> allPoints = new ArrayList<>();
        allPoints.add(request.getOrigin());
        if (request.getWaypoints() != null) {
            allPoints.addAll(request.getWaypoints());
        }
        allPoints.add(request.getDestination());

        return allPoints;
    }

    /**
     * api 호출 에러 시, 구간별 거리, 시간 계산
     * @param request
     * @return
     */
    private DistanceResponse getDistanceBySegments(CarDistanceRequest request) {
        System.out.println("구간별 계산 시작");

        double totalDistance = 0;
        double totalDuration = 0;

        // 전체 경로 점들 (origin + waypoints + destination)
        List<Point> allPoints = buildAllPoints(request);

        // 각 구간별로 계산
        for (int i = 0; i < allPoints.size() - 1; i++) {
            Point start = allPoints.get(i);
            Point end = allPoints.get(i + 1);

            System.out.println("구간 " + (i+1) + " 계산: (" + start.getX() + "," + start.getY() + ") → (" + end.getX() + "," + end.getY() + ")");
            
            try {
                // 구간별 단순 경로 요청(waypoints 없이)
                CarDistanceRequest segmentRequest = new CarDistanceRequest();
                segmentRequest.setOrigin(start);
                segmentRequest.setDestination(end);

                DistanceResponse segmentResponse = getDistance(segmentRequest);
                totalDistance += segmentResponse.getDistance();
                totalDuration += segmentResponse.getDuration();
                
                System.out.println("구간 " + (i+1) + " 성공: " + segmentResponse.getDistance() + "m, " + segmentResponse.getDuration() + "s");
            } catch (Exception e) {
                System.out.println("구간 " + (i+1) + " 실패, 직선거리 추정 사용: " + e.getMessage());
                
                // 직선 거리 계산(하버사인 공식)
                double straightDistance = calculateStraightDistance(start, end);
                double estimatedDistance = straightDistance * 1.3; // 도로 보정계수
                double estimatedDuration = estimatedDistance / (50 * 1000 / 3600); // 평균 50km/h 가정, 초 단위

                totalDistance += estimatedDistance;
                totalDuration += estimatedDuration;
                
                System.out.println("구간 " + (i+1) + " 추정: " + estimatedDistance + "m, " + estimatedDuration + "s");
            }
        }

        double carbonEmission = CarbonCalculator.CalculateCarCarbonEmission(totalDistance);
        System.out.println("구간별 계산 완료 - 총 거리: " + totalDistance + "m, 총 시간: " + totalDuration + "s, 탄소배출량: " + carbonEmission + "g");

        return new DistanceResponse(totalDistance, (int) totalDuration, carbonEmission);
    }

    /**
     * 직선 거리 구하기
     * @param start
     * @param end
     * @return
     */
    private double calculateStraightDistance(Point start, Point end) {
        final double R = 6371000; // 지구 반지름 (미터)
        double lat1Rad = Math.toRadians(start.getY());
        double lat2Rad = Math.toRadians(end.getY());
        double deltaLatRad = Math.toRadians(end.getY() - start.getY());
        double deltaLonRad = Math.toRadians(end.getX() - start.getX());

        double a = Math.sin(deltaLatRad/2) * Math.sin(deltaLatRad/2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLonRad/2) * Math.sin(deltaLonRad/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }
}
