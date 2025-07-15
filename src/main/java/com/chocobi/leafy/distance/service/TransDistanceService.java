package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.TmapPathTypeConst;
import com.chocobi.leafy.distance.domain.TransDistanceRequest;
import com.chocobi.leafy.distance.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TransDistanceService {

    private final WebClient tmapWebClient;

    public TransDistanceService(WebClient tmapWebClient) {
        this.tmapWebClient = tmapWebClient;
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     *
     * @param request
     * @return
     */
    public List<RouteCalculationResult> getDistance(TransDistanceRequest request) {

        String uri = "/transit/routes";

        // 티맵 대중교통 api 호출
        TmapResponse tmapResponse = tmapWebClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TmapResponse.class)
                .block(); // 동기 호출

        // 응답이 존재하는 지 확인
        if (tmapResponse == null || tmapResponse.getMetaData() == null) {
            throw new RuntimeException("TMap 대중교통 api 응답 실패, tmapResponse = " + tmapResponse);
        }

        // MetaData에 있는 Plan 꺼내기
        MetaData metaData = tmapResponse.getMetaData();
        Plan plan = metaData.getPlan();

        // plan이 존재하는지 확인
        if (plan == null) {
            throw new RuntimeException("Plan 객체가 없습니다.");
        }

        // 경로안 꺼내기
        List<Itineraries> itineraries = plan.getItineraries();

        // 경로안이 존재하는 지 확인
        if (itineraries == null || itineraries.isEmpty()) {
            throw new RuntimeException("경로안(itineraries)가 없습니다.");
        }

        // 경로안을 순회하면서 pathType이 AIRPLANE인 경우는 제외
        List<RouteCalculationResult> finalResults = new ArrayList<>();
        boolean[] added = new boolean[5]; // 각 방법이 이미 추가되었는지
        Arrays.fill(added, false);

        for (Itineraries itinerary : itineraries) {
            int pathType = itinerary.getPathType();

            if (pathType != TmapPathTypeConst.AIRPLANE && !added[pathType - 1]) {
                RouteCalculationResult result = new RouteCalculationResult();
                result.setPathType((pathType));
                result.setTotalTime(itinerary.getTotalTime());

                int totalDistance = itinerary.getTotalDistance();
                int totalWalkDistance = itinerary.getTotalWalkDistance();

                for (Legs leg : itinerary.getLegs()) {
                    String mode = leg.getMode();
                    Integer distance = leg.getDistance();

                    if (distance != null) {
                        switch (mode) {
                            case "BUS", "EXPRESSBUS":
                                result.setBusDistance(result.getBusDistance() + distance);
                                break;
                            case "TRAIN":
                                result.setTrainDistance(result.getTrainDistance() + distance);
                                break;
                        }
                    }
                }

                int totalSubwayDistance = totalDistance - totalWalkDistance - result.getBusDistance() - result.getTrainDistance();
                result.setSubwayDistance(Math.max(0, totalSubwayDistance));

                finalResults.add(result);

                added[pathType - 1] = true;
            }
        }

        System.out.println("최종 계산 결과:" + finalResults);

        // 상세 경로 넘겨줄 거면 리턴값 다시 생각해봐야함
        return finalResults;
    }
}
