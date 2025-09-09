package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.constants.CarbonEmissionConst;
import com.chocobi.leafy.constants.DistanceConst;
import com.chocobi.leafy.constants.TmapPathTypeConst;
import com.chocobi.leafy.distance.domain.TransDistanceRequest;
import com.chocobi.leafy.distance.domain.TransDistanceBatchRequest;
import com.chocobi.leafy.distance.dto.*;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.service.TripPlaceService;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransDistanceService {

    private final WebClient tmapWebClient;
    private final TripPlaceService tripPlaceService;
    private final PlaceService placeService;

    /**
     * 여러 구간의 대중교통 경로를 배치로 처리하는 메서드
     */
    public List<RouteCalculationResult> getBatchDistance(TransDistanceBatchRequest batchRequest) {

        List<RouteCalculationResult> allResults = new ArrayList<>();

        try {
            // TripPlace 정보를 가져와서 순서대로 정렬
            List<TripPlaceResponse> tripPlaces = new ArrayList<>(tripPlaceService.getTripPlaces(batchRequest.getTripId()));
            tripPlaces.sort((a, b) -> Integer.compare(a.getVisitOrder(), b.getVisitOrder()));

            // 제주 여행인지 확인
            boolean isJejuTrip = isJejuTrip(tripPlaces);

            // 각 구간마다 개별적으로 getDistance 호출 (순수 계산만)
            List<TransDistanceRequest> requests = batchRequest.getRequests();
            for (TransDistanceRequest request : requests) {
                RouteCalculationResult segmentResult = getDistance(request, isJejuTrip);
                if (segmentResult != null) {
                    allResults.add(segmentResult);
                }
            }

            return allResults;

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 두 좌표 사이의 거리와 시간 정보를 얻어오는 메서드
     * /@param request
     * /@return
     */
    public RouteCalculationResult getDistance(TransDistanceRequest request, boolean isJejuTrip) {

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

        // 경로 선택 및 maxCarbonEmission 계산용 항공 경로 확인
        Itineraries selectedItinerary = null;
        double maxCarbonEmission = 0;
        
        // 제주가 아닌 경우: 항공 경로와 대중교통 경로 모두 확인
        if (!isJejuTrip) {
            Itineraries airplaneItinerary = null;
            Itineraries publicItinerary = null;
            
            for (Itineraries itinerary : itineraries) {
                int pathType = itinerary.getPathType();
                if (pathType == TmapPathTypeConst.AIRPLANE && airplaneItinerary == null) {
                    airplaneItinerary = itinerary;
                } else if (pathType != TmapPathTypeConst.AIRPLANE && publicItinerary == null) {
                    publicItinerary = itinerary;
                }
            }
            
            // 항공 경로가 있으면 탄소 배출량 계산 (maxCarbonEmission용)
            if (airplaneItinerary != null) {
                RouteCalculationResult airplaneResult = createRouteResult(airplaneItinerary);
                maxCarbonEmission = airplaneResult.getCarbonEmission();
            }
            
            // 대중교통 경로를 실제 반환용으로 선택
            selectedItinerary = publicItinerary != null ? publicItinerary : itineraries.getFirst();
        } else {
            // 제주 여행인 경우: 첫 번째 경로 사용
            selectedItinerary = itineraries.getFirst();
        }

        if (selectedItinerary == null) {
            throw new RuntimeException("유효한 경로를 찾을 수 없습니다.");
        }

        // 선택된 경로로 결과 생성
        RouteCalculationResult result = createRouteResult(selectedItinerary);
        
        // maxCarbonEmission 설정 (제주가 아닌 경우)
        if (!isJejuTrip && maxCarbonEmission > 0) {
            result.setMaxCarbonEmission(Math.max(result.getCarbonEmission(), maxCarbonEmission));
        } else {
            result.setMaxCarbonEmission(result.getCarbonEmission());
        }

        return result;
    }

    private boolean isJejuTrip(List<TripPlaceResponse> tripPlaces) {
        return tripPlaces.stream()
                .anyMatch(tripPlace -> {
                    Place place = placeService.getPlaceById(tripPlace.getPlaceId());

                    return place.getAddress() != null && place.getAddress().contains("제주");
                });
    }

    private RouteCalculationResult createRouteResult(Itineraries itinerary) {
        int pathType = itinerary.getPathType();
        RouteCalculationResult result = new RouteCalculationResult();
        result.setPathType(pathType);
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
                    case "AIRPLANE":
                        result.setAirplaneDistance(result.getAirplaneDistance() + distance);
                        break;
                }
            }
        }
        
        // 지하철 거리 계산
        int totalSubwayDistance;
        if (pathType == TmapPathTypeConst.AIRPLANE) {
            totalSubwayDistance = totalDistance - totalWalkDistance - result.getBusDistance() - result.getTrainDistance() - result.getAirplaneDistance();
        } else {
            totalSubwayDistance = totalDistance - totalWalkDistance - result.getBusDistance() - result.getTrainDistance();
        }
        result.setSubwayDistance(Math.max(0, totalSubwayDistance));

        // 탄소 배출량 계산
        double carbonEmission = (result.getSubwayDistance() / 1000.0) * CarbonEmissionConst.SUBWAY_EMISSION + 
                               (result.getTrainDistance() / 1000.0) * CarbonEmissionConst.TRAIN_EMISSION + 
                               (result.getBusDistance() / 1000.0) * CarbonEmissionConst.BUS_EMISSION;
        
        if (pathType == TmapPathTypeConst.AIRPLANE) {
            carbonEmission += (result.getAirplaneDistance() / 1000.0) * CarbonEmissionConst.AIRPLANE_EMISSION;
        }
        
        result.setCarbonEmission(carbonEmission);
        
        return result;
    }
}