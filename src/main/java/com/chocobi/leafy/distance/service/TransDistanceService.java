package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.CarbonEmissionConst;
import com.chocobi.leafy.constants.DistanceConst;
import com.chocobi.leafy.constants.TmapPathTypeConst;
import com.chocobi.leafy.distance.domain.TransDistanceRequest;
import com.chocobi.leafy.distance.domain.TransDistanceBatchRequest;
import com.chocobi.leafy.distance.dto.*;
import com.chocobi.leafy.trip.service.TripSegmentService;
import com.chocobi.leafy.trip.service.TripPlaceService;
import com.chocobi.leafy.trip.dto.TripSegmentRedisDto;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TransDistanceService {

    private final WebClient tmapWebClient;
    private final TripSegmentService tripSegmentService;
    private final TripPlaceService tripPlaceService;

    public TransDistanceService(WebClient tmapWebClient, TripSegmentService tripSegmentService, TripPlaceService tripPlaceService) {
        this.tmapWebClient = tmapWebClient;
        this.tripSegmentService = tripSegmentService;
        this.tripPlaceService = tripPlaceService;
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     * /@param request
     * /@return
     */
    public List<RouteCalculationResult> getDistance(TransDistanceRequest request) {

        // 티맵 대중교통 api 호출
        
        TmapResponse tmapResponse = tmapWebClient.post()
                .uri(DistanceConst.tmapUri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TmapResponse.class)
                .block(); // 동기 호출
                

        // 응답이 존재하는 지 확인
        if (tmapResponse == null) {
            throw new RuntimeException("TMap 대중교통 api 응답 실패, tmapResponse = null");
        }
        
        // 검색 결과가 없는 경우 확인
        if (tmapResponse.getResult() != null && tmapResponse.getResult().getStatus() == 14) {
            throw new RuntimeException("대중교통 경로 검색 결과가 없습니다: " + tmapResponse.getResult().getMessage());
        }
        
        if (tmapResponse.getMetaData() == null) {
            throw new RuntimeException("TMap 대중교통 api 응답 실패, metaData = null");
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

        // 최적 경로(첫 번째) 하나만 선택
        List<RouteCalculationResult> finalResults = new ArrayList<>();

        // 첫 번째 유효한 경로만 사용
        for (Itineraries itinerary : itineraries) {
            int pathType = itinerary.getPathType();

            if (pathType != TmapPathTypeConst.AIRPLANE) {
                RouteCalculationResult result = new RouteCalculationResult();
                result.setPathType((pathType));
                result.setTotalTime(itinerary.getTotalTime());

                int totalDistance = itinerary.getTotalDistance();
                result.setTotalDistance(totalDistance);
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

                
                double carbonEmission = (result.getSubwayDistance() / 1000.0) * CarbonEmissionConst.SUBWAY_EMISSION + (result.getTrainDistance() / 1000.0) * CarbonEmissionConst.TRAIN_EMISSION + (result.getBusDistance() / 1000.0) * CarbonEmissionConst.BUS_EMISSION;
                
                result.setCarbonEmission(carbonEmission);

                finalResults.add(result);
                
                // 첫 번째 유효한 경로만 사용하고 루프 종료
                break;
            }
        }


        // 상세 경로 넘겨줄 거면 리턴값 다시 생각해봐야함
        return finalResults;
    }

    /**
     * TripSegmentRedisDto 생성을 포함한 대중교통 경로 계산 메서드
     */
    public List<RouteCalculationResult> getDistance(TransDistanceRequest request, Long tripId, Long startPlaceId, Long endPlaceId) {
        List<RouteCalculationResult> results;
        
        try {
            results = getDistance(request);
        } catch (Exception e) {
            // API 실패 시 추정값으로 폴백
            RouteCalculationResult estimatedResult = new RouteCalculationResult();
            estimatedResult.setPathType(1); // 기본 대중교통 타입
            estimatedResult.setTotalDistance(50000); // 50km 추정
            estimatedResult.setTotalTime(3000); // 50분 추정
            estimatedResult.setCarbonEmission(2.5); // 추정 탄소 배출량
            results = List.of(estimatedResult);
        }
        
        // 최적 경로의 탄소 배출량으로 TripSegmentRedisDto 생성
        if (!results.isEmpty()) {
            RouteCalculationResult bestRoute = results.get(0); // 첫 번째가 최적 경로
            
            TripSegmentRedisDto tripSegmentDto = TripSegmentRedisDto.builder()
                    .tripId(tripId)
                    .startPlaceId(startPlaceId)
                    .endPlaceId(endPlaceId)
                    .transport("대중교통")
                    .distance(bestRoute.getTotalDistance())
                    .carbonEmitted(bestRoute.getCarbonEmission())
                    .build();
                    
            // Redis에 개별 저장 (기존 리스트에 추가)
            saveSingleTripSegment(tripSegmentDto);
        }
        
        return results;
    }
    
    /**
     * 개별 TripSegmentRedisDto를 Redis에 저장
     */
    private void saveSingleTripSegment(TripSegmentRedisDto tripSegmentDto) {
        List<TripSegmentRedisDto> existingList;
        
        try {
            existingList = tripSegmentService.getTempTripSegments(tripSegmentDto.getTripId());
        } catch (IllegalArgumentException e) {
            // 기존 데이터가 없는 경우 새로운 리스트 생성
            existingList = new ArrayList<>();
        }
        
        existingList.add(tripSegmentDto);
        tripSegmentService.saveTempTripSegments(existingList);
    }

    /**
     * 여러 구간의 대중교통 경로를 배치로 처리하는 메서드
     */
    public List<RouteCalculationResult> getBatchDistance(TransDistanceBatchRequest batchRequest) {
        
        List<RouteCalculationResult> allResults = new ArrayList<>();
        
        try {
            // TripPlace 정보를 가져와서 순서대로 정렬
            List<TripPlaceResponse> tripPlaces = new ArrayList<>(tripPlaceService.getTripPlaces(batchRequest.getTripId()));
            tripPlaces.sort((a, b) -> Integer.compare(a.getVisitOrder(), b.getVisitOrder()));
            
            // 각 구간마다 개별적으로 getDistance 호출 (모든 요청 처리)
            List<TransDistanceRequest> requests = batchRequest.getRequests();
            for (int i = 0; i < requests.size(); i++) {
                TransDistanceRequest request = requests.get(i);
                
                // 여행지 간 구간만 TripSegmentRedisDto 생성 (출발지→출발지 구간 제외)
                if (i < tripPlaces.size() - 1) {
                    Long startPlaceId = tripPlaces.get(i).getPlaceId();
                    Long endPlaceId = tripPlaces.get(i + 1).getPlaceId();
                    
                    // TripSegmentRedisDto 생성을 포함한 getDistance 호출
                    List<RouteCalculationResult> segmentResults = getDistance(request, batchRequest.getTripId(), startPlaceId, endPlaceId);
                    allResults.addAll(segmentResults);
                } else {
                    // 출발지↔목적지 구간은 결과만 반환 (TripSegment 저장 안함)
                    List<RouteCalculationResult> segmentResults = getDistance(request);
                    allResults.addAll(segmentResults);
                }
            }
            
            return allResults;
            
        } catch (Exception e) {
            throw e;
        }
    }
}