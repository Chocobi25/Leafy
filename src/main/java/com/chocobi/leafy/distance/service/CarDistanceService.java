package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.DistanceConst;
import com.chocobi.leafy.constants.PortConst;
import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.Point;
import com.chocobi.leafy.distance.domain.Port;
import com.chocobi.leafy.distance.dto.*;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.util.CarbonCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static com.chocobi.leafy.constants.PortConst.isFerrySection;
import static com.chocobi.leafy.distance.service.DistanceUtils.findNearestPort;
import static com.chocobi.leafy.distance.service.DistanceUtils.placeToPoint;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarDistanceService {
    private final PlaceService placeService;
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
        int totalDistance = 0;
        int duration = summary.getDuration();
        double totalCarbonEmission = 0.0;

        // 카카오에서 제공하는 section 구간별 처리
        List<Section> sections = routes.getSections();

        // 전체 경로의 모든 점들을 순서대로 담은 리스트를 만듭니다.
        // 이 리스트를 기준으로 sections를 처리해야 인덱스 에러가 발생하지 않습니다.
        List<Point> allPoints = new ArrayList<>();
        allPoints.add(request.getOrigin());
        if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
            allPoints.addAll(request.getWaypoints());
        }
        allPoints.add(request.getDestination());

        // allPoints 리스트를 순회하며 각 구간의 탄소량을 계산합니다.
        // sections 리스트와 allPoints 리스트의 크기는 항상 일치해야 합니다.
        if (sections.size() != allPoints.size() - 1) {
            log.error("API에서 반환된 섹션의 개수가 예상과 다릅니다. 예상: {}, 실제: {}", allPoints.size() - 1, sections.size());
            // 이 경우, 구간별 계산 fallback 로직을 호출하거나 예외를 던질 수 있습니다.
            // 여기서는 예외를 던지는 것으로 가정합니다.
            throw new RuntimeException("경로 섹션과 경유지 목록의 불일치 오류. 경로를 다시 확인해주세요.");
        }

        // 이제 인덱스 에러 없이 안전하게 루프를 실행할 수 있습니다.
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);

            // allPoints 리스트에서 해당 섹션의 시작점과 끝점을 가져옵니다.
            Point startPoint = allPoints.get(i);
            Point endPoint = allPoints.get(i + 1);

            double sectionCarbonEmission;

            // 페리 구간인지 판별
            if (isFerrySection(startPoint, endPoint)) {
                sectionCarbonEmission = CarbonCalculator.CalculateFerryCarbonEmission(section.getDistance());
            } else {
                sectionCarbonEmission = CarbonCalculator.CalculateCarCarbonEmission(section.getDistance());
            }

            section.setCarbonEmission(sectionCarbonEmission);
            section.setMaxCarbonEmission(sectionCarbonEmission);

            totalCarbonEmission += sectionCarbonEmission;
            totalDistance += section.getDistance();
        }

        CarDistanceResponse carDistanceResponse = new CarDistanceResponse();
        carDistanceResponse.setDistanceResponse(new DistanceResponse(totalDistance, duration, totalCarbonEmission));
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

    public CarDistanceRequest addPortsToRequest(CarDistanceRequest request, List<TripPlaceResponse> tripPlaces) {
        log.info("addPortsToRequest 호출: 제주도 경유지 확인 시작");

        // 1. 제주도 장소의 첫 번째와 마지막 인덱스 찾기
        int firstJejuIndex = -1;
        int lastJejuIndex = -1;

        // TripPlaces의 전체 목록을 로그로 출력 (디버깅 용)
        log.debug("전체 tripPlaces 목록: {}", tripPlaces);

        for (int i = 0; i < tripPlaces.size(); i++) {
            Place place = placeService.getPlaceById(tripPlaces.get(i).getPlaceId());
            if (place.getAddress() != null && place.getAddress().contains("제주")) {
                if (firstJejuIndex == -1) {
                    firstJejuIndex = i;
                }
                lastJejuIndex = i;
            }
        }

        // 제주도 장소가 없다면 아무것도 하지 않고 기존 요청 반환
        if (firstJejuIndex == -1) {
            log.info("제주도 경유지가 없어 항구를 추가하지 않습니다.");
            return request;
        }

        log.info("제주도 경유지 발견: 첫 번째 인덱스 = {}, 마지막 인덱스 = {}", firstJejuIndex, lastJejuIndex);

        // 2. 항구를 추가할 새로운 Waypoints 리스트 생성
        List<Point> newWaypoints = new ArrayList<>();

        // 3. 육지에서 제주로 가는 항구 추가
        Point preJejuPoint = (firstJejuIndex > 0)
                ? placeToPoint(placeService.getPlaceById(tripPlaces.get(firstJejuIndex - 1).getPlaceId()))
                : request.getOrigin();

        log.info("제주도 진입 전 마지막 장소의 좌표: {}", preJejuPoint);
        Port nearestDepartPort = findNearestPort(preJejuPoint, PortConst.JEJU_DEPART_PORTS);
        newWaypoints.add(nearestDepartPort.getPoint());
        log.info("선택된 육지 출발 항구: {} ({})", nearestDepartPort.getName(), nearestDepartPort.getPoint());

        Point firstJejuPoint = placeToPoint(placeService.getPlaceById(tripPlaces.get(firstJejuIndex).getPlaceId()));
        Port nearestJejuArrivePort = findNearestPort(firstJejuPoint, PortConst.JEJU_ARRIVE_PORTS);
        newWaypoints.add(nearestJejuArrivePort.getPoint());
        log.info("선택된 제주 도착 항구: {} ({})", nearestJejuArrivePort.getName(), nearestJejuArrivePort.getPoint());

        // 4. 제주도 내 기존 경유지 추가
        for (int i = firstJejuIndex; i <= lastJejuIndex; i++) {
            newWaypoints.add(placeToPoint(placeService.getPlaceById(tripPlaces.get(i).getPlaceId())));
        }
        log.debug("제주도 내 경유지 추가 완료. 현재 waypoints: {}", newWaypoints);

        // 5. 제주도에서 육지로 돌아오는 항구 추가 (무조건 추가)
        Point lastJejuPoint = placeToPoint(placeService.getPlaceById(tripPlaces.get(lastJejuIndex).getPlaceId()));
        Port nearestJejuDepartPort = findNearestPort(lastJejuPoint, PortConst.JEJU_ARRIVE_PORTS);
        newWaypoints.add(nearestJejuDepartPort.getPoint());
        log.info("선택된 제주 출발 항구: {} ({})", nearestJejuDepartPort.getName(), nearestJejuDepartPort.getPoint());

        // 다음 경유지 또는 최종 목적지
        Point postJejuPoint = (lastJejuIndex < tripPlaces.size() - 1)
                ? placeToPoint(placeService.getPlaceById(tripPlaces.get(lastJejuIndex + 1).getPlaceId()))
                : request.getDestination();
        Port nearestArrivePort = findNearestPort(postJejuPoint, PortConst.JEJU_DEPART_PORTS);
        newWaypoints.add(nearestArrivePort.getPoint());
        log.info("선택된 육지 도착 항구: {} ({})", nearestArrivePort.getName(), nearestArrivePort.getPoint());

        log.debug("제주도 내 경유지 추가 완료. 현재 waypoints: {}", newWaypoints);

        // 6. 제주도 외의 나머지 경유지 추가
        for (int i = lastJejuIndex + 1; i < tripPlaces.size(); i++) {
            newWaypoints.add(placeToPoint(placeService.getPlaceById(tripPlaces.get(i).getPlaceId())));
        }

        // 7. 기존 waypoints를 새로 구성된 리스트로 교체
        request.setWaypoints(newWaypoints);

        log.info("최종적으로 구성된 waypoints 리스트: {}", newWaypoints);

        // 8. 수정된 요청 객체 반환
        return request;
    }
}