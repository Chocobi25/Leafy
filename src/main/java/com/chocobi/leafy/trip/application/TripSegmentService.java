package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.distance.domain.*;
import com.chocobi.leafy.distance.dto.CarDistanceResponse;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.service.DistanceUtils;
import com.chocobi.leafy.distance.service.TransDistanceService;
import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripSegmentDTO;
import com.chocobi.leafy.trip.dto.TripSegmentRedisDto;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripSegmentCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.chocobi.leafy.distance.service.DistanceUtils.placeToPoint;
import static com.chocobi.leafy.distance.service.DistanceUtils.regionToPoint;

@Service
@RequiredArgsConstructor
public class TripSegmentService {
    private final TripSegmentFindService tripSegmentFindService;
    private final TripSegmentCommandService tripSegmentCommandService;
    private final TripFindService tripFindService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CarDistanceService carDistanceService;
    private final TransDistanceService transDistanceService;
    private final TripPlaceService tripPlaceService;
    private final PlaceService placeService;

    /**
     * TripSegmentRedisDto를 만들고 Redis에 임시 저장하는 편의 통합 메서드
     *
     * @param tripId
     * @param sections
     * @param transport
     * @param tripPlaces
     */
    public void completeTempTripSegments(Long tripId, List<Section> sections, String transport, List<TripPlaceResponse> tripPlaces) {
        List<TripSegmentRedisDto> tripSegmentDtos = createTripSegmentRedisDto(tripId, sections, transport, tripPlaces);
        saveTempTripSegments(tripSegmentDtos);
    }

    /**
     * TripSegmentRedisDto 생성 메서드
     */
    private List<TripSegmentRedisDto> createTripSegmentRedisDto(Long tripId, List<Section> sections, String transport, List<TripPlaceResponse> tripPlaces) {
        List<TripSegmentRedisDto> tripSegmentDtos = new ArrayList<>();

        if (tripPlaces == null || tripPlaces.size() < 2) return tripSegmentDtos;

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        // sections 길이 체크: 보통 sections.size() == tripPlaces.size()-1 이어야 함
        for (int i = 0; i < mutableTripPlaces.size() - 1; i++) {
            // 안전하게 섹션 인덱스 검사
            if (i >= sections.size()) break;

            TripPlaceResponse startPlace = mutableTripPlaces.get(i);
            TripPlaceResponse endPlace = mutableTripPlaces.get(i + 1);
            double distance = sections.get(i).getDistance();
            int durationInMinutes = Math.max(1, sections.get(i).getDuration() / 60); // 초 -> 분
            double carbonEmission = sections.get(i).getCarbonEmission();
            double maxCarbonEmission = sections.get(i).getMaxCarbonEmission();

            TripSegmentRedisDto dto = TripSegmentRedisDto.builder()
                    .tripId(tripId)
                    .startTripPlaceId(startPlace.getTripPlaceId())
                    .endTripPlaceId(endPlace.getTripPlaceId())
                    .transport(transport == null ? null : transport.toLowerCase())
                    .distance(distance)
                    .duration(durationInMinutes)
                    .carbonEmitted(carbonEmission)
                    .maxCarbonEmission(maxCarbonEmission)
                    .build();

            tripSegmentDtos.add(dto);
        }

        return tripSegmentDtos;
    }

    /**
     * Redis에 TripSegmentRedisDto 임시 저장
     */
    public Long saveTempTripSegments(List<TripSegmentRedisDto> tripSegmentDtos) {

        if (tripSegmentDtos == null || tripSegmentDtos.isEmpty()) {
            throw new IllegalArgumentException("TripSegmentDtos가 비어있습니다.");
        }

        Long tripId = tripSegmentDtos.get(0).getTripId();
        String transport = tripSegmentDtos.get(0).getTransport();
        if (transport == null) {
            throw new IllegalArgumentException("transport가 null입니다. 저장 시 교통수단을 명시해주세요.");
        }
        transport = transport.toLowerCase();

        String key = "temp_trip_segments:" + tripId + ":" + transport;
        redisTemplate.opsForValue().set(key, tripSegmentDtos);
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);

        return tripId;
    }

    /**
     * 트립 세그먼트 만들기 (DB용 엔티티 리스트)
     */
    public List<TripSegmentEntity> createTripSegments(Long tripId, List<Section> sections, String transport, List<TripPlaceResponse> tripPlaces) {
        List<TripSegmentEntity> tripSegments = new ArrayList<>();

        if (tripPlaces == null || tripPlaces.size() < 2) return tripSegments;

        TripEntity trip = tripFindService.findTrip(tripId);

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        for (int i = 0; i < mutableTripPlaces.size() - 1; i++) {
            if (i >= sections.size()) break;

            TripPlaceResponse startPlace = mutableTripPlaces.get(i);
            TripPlaceResponse endPlace = mutableTripPlaces.get(i + 1);
            double distance = sections.get(i).getDistance();
            int durationInMinutes = Math.max(1, sections.get(i).getDuration() / 60);
            double carbonEmission = sections.get(i).getCarbonEmission();
            double maxCarbonEmission = sections.get(i).getMaxCarbonEmission();
            TripPlaceEntity startTripPlace = tripPlaceService.getTripPlaceById(startPlace.getTripPlaceId());
            TripPlaceEntity endTripPlace = tripPlaceService.getTripPlaceById(endPlace.getTripPlaceId());

            TripSegmentEntity tripSegment = TripSegmentEntity.builder()
                    .trip(trip)
                    .startTripPlace(startTripPlace)
                    .endTripPlace(endTripPlace)
                    .transport(transport == null ? null : transport.toLowerCase())
                    .distance(distance)
                    .duration(durationInMinutes)
                    .carbonEmitted(carbonEmission)
                    .maxCarbonEmission(maxCarbonEmission)
                    .build();
            tripSegments.add(tripSegment);
        }

        return tripSegments;
    }

    /**
     * 임시 TripSegments를 DB에 저장하고 Redis에서 삭제
     */
    public void completeTripSegments(Long tripId, String transport) {
        if (transport == null) throw new IllegalArgumentException("transport가 필요합니다.");

        String normalized = transport.toLowerCase();
        List<TripSegmentRedisDto> tripSegmentDtos = getTempTripSegments(tripId, normalized); // 임시 TripSegmentRedisDto를 불러와서

        String otherTransport = normalized.equals("car") ? "public" : "car";
        List<TripSegmentRedisDto> otherTripSegmentDtos = null;

        try {
            otherTripSegmentDtos = getTempTripSegments(tripId, otherTransport);
        } catch (IllegalArgumentException e) {
            // 다른 교통수단이 없을 수 있음 — 무시
        }

        // 최종 선택된 교통수단으로 transport 값 업데이트 및 maxCarbonEmission 설정
        List<TripSegmentEntity> tripSegments = new ArrayList<>();
        for (int i = 0; i < tripSegmentDtos.size(); i++) {
            TripSegmentRedisDto dto = tripSegmentDtos.get(i);
            dto.setTransport(normalized);

            // maxCarbonEmission 설정: 현재 교통수단과 다른 교통수단의 maxCarbonEmission 중 더 큰 값
            double maxCarbon = dto.getMaxCarbonEmission();
            if (otherTripSegmentDtos != null && i < otherTripSegmentDtos.size()) {
                double otherMaxCarbon = otherTripSegmentDtos.get(i).getMaxCarbonEmission();
                maxCarbon = Math.max(maxCarbon, otherMaxCarbon);
            }
            dto.setMaxCarbonEmission(maxCarbon);

            tripSegments.add(dto.toEntity(
                    tripPlaceService.getTripPlaceById(dto.getStartTripPlaceId()),
                    tripPlaceService.getTripPlaceById(dto.getEndTripPlaceId())
            ));
        }

        saveTripSegments(tripSegments); // DB에 저장
        deleteTempTripSegments(tripId, normalized); // 임시 저장한 TripSegments를 삭제
        deleteTempTripSegments(tripId, otherTransport);
    }

    /**
     * Redis에서 임시 TripSegmentRedisDto 가져오기
     */
    @SuppressWarnings("unchecked")
    public List<TripSegmentRedisDto> getTempTripSegments(Long tripId, String transport) {
        if (transport == null) transport = "car";
        String normalized = transport.toLowerCase();
        String key = "temp_trip_segments:" + tripId + ":" + normalized;
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            throw new IllegalArgumentException("임시 저장된 TripSegments가 없습니다. tripId: " + tripId + ", transport: " + normalized);
        }

        return (List<TripSegmentRedisDto>) result;
    }

    /**
     * Redis에서 TripSegmentRedisDto 삭제
     */
    public void deleteTempTripSegments(Long tripId, String transport) {
        if (transport == null) return;
        String key = "temp_trip_segments:" + tripId + ":" + transport.toLowerCase();
        redisTemplate.delete(key);
    }

    /**
     * Redis에 저장된 TripSegment들의 총 시간과 탄소배출량 계산
     */
    public Map<String, Object> getTotalTimeAndCarbon(Long tripId, String transport) {
        List<TripSegmentRedisDto> segments = getTempTripSegments(tripId, transport);

        int totalDuration = segments.stream()
                .mapToInt(TripSegmentRedisDto::getDuration)
                .sum();

        double totalCarbonEmission = segments.stream()
                .mapToDouble(TripSegmentRedisDto::getCarbonEmitted)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("totalDuration", totalDuration); // 분 단위
        result.put("totalCarbonEmission", totalCarbonEmission); // g 단위

        return result;
    }

    /**
     * DB에 TripSegment 저장
     */
    public void saveTripSegments(List<TripSegmentEntity> tripSegments) {
        if (tripSegments == null || tripSegments.isEmpty()) return;
        tripSegmentCommandService.saveAll(tripSegments);
    }

    /**
     * 자동차 경로 계산 및 Redis 저장 통합 메서드
     */
    @Transactional
    public DistanceResponse calculateAndSaveCarRoute(CarDistanceRequest request, Long tripId) {
        // 서비스 내부에서 tripPlaces를 가져옵니다.
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);

        CarDistanceResponse carResponse;
        CarDistanceRequest finalRequest = request;

        // 제주도 여행 여부 판별 및 항구 포함 처리
        if (DistanceUtils.isJejuTrip(tripPlaces)) {
            CarDistanceRequest modifiedRequest = carDistanceService.addPortsToRequest(request, tripPlaces);
            carResponse = carDistanceService.getDistance(modifiedRequest);
        } else {
            carResponse = carDistanceService.getDistance(request);
        }

        // sections 가져오기
        List<Section> sections = carResponse.getSections();

        // Redis에 저장 (transport 는 "car")
        completeTempTripSegments(tripId, sections, "car", tripPlaces);

        return carResponse.getDistanceResponse();
    }

    /**
     * 대중교통 경로 계산 및 Redis 저장 통합 메서드
     */
    public List<RouteCalculationResult> calculateAndSavePublicRoute(TransDistanceBatchRequest batchRequest, List<TripPlaceResponse> tripPlaces) {
        List<RouteCalculationResult> results = transDistanceService.getBatchDistance(batchRequest);

        // RouteCalculationResult를 Section으로 변환
        List<Section> sections = new ArrayList<>();
        for (RouteCalculationResult result : results) {
            Section section = new Section();
            section.setDistance((int) result.getTotalDistance());
            section.setDuration(result.getTotalTime());
            section.setCarbonEmission(result.getCarbonEmission());
            section.setMaxCarbonEmission(result.getMaxCarbonEmission());
            sections.add(section);
        }

        // Redis에 저장 (transport 는 "public")
        completeTempTripSegments(batchRequest.getTripId(), sections, "public", tripPlaces);

        return results;
    }

    @Transactional
    public List<TripSegmentDTO> getTripSegments(Long tripId) {
        return tripSegmentFindService.findTripSegmentsByTripId(tripId).stream()
                .map(TripSegmentDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteTripSegments(TripEntity trip) {
        tripSegmentCommandService.deleteAllByTrip(trip);
    }

    /**
     * 재계산 진입점: 프론트에서 온 tripPlaceRequests 를 TripPlaceResponse로 변환한 뒤
     * 적절한 거리 서비스 메서드를 호출(그 내부에서 Redis 저장까지 수행).
     */
    @Transactional
    public void recalculateRoutesAndSave(TripEntity trip, String transport, List<TripPlaceRequest> tripPlaceRequests) {
        List<TripPlaceResponse> tripPlaces = tripPlaceRequests.stream()
                .map(req -> TripPlaceResponse.builder()
                        .tripId(trip.getId())
                        .place(PlaceDTO.fromEntity(placeService.getPlace(req.getPlaceId())))
                        .dayIndex(req.getDayIndex())
                        .visitOrder(req.getVisitOrder())
                        .memo(req.getMemo())
                        .build())
                .toList();

        System.out.println("[DEBUG] TripPlaces to recalc: " + tripPlaces);

        String normalized = transport == null ? "car" : transport.toLowerCase();
        if ("car".equals(normalized)) {
            // build car request and call calculateAndSaveCarRoute which stores to redis
            CarDistanceRequest carRequest = new CarDistanceRequest();
            carRequest.setTripId(trip.getId());

            if (!tripPlaces.isEmpty()) {
                PlaceDTO firstPlace = tripPlaces.get(0).getPlace();
                PlaceDTO lastPlace = tripPlaces.get(tripPlaces.size() - 1).getPlace();

                carRequest.setOrigin(placeToPoint(firstPlace));
                carRequest.setDestination(placeToPoint(lastPlace));
            }

            if (tripPlaces.size() > 2) {
                carRequest.setWaypoints(
                        tripPlaces.subList(1, tripPlaces.size() - 1)
                                .stream()
                                .map(tp -> placeToPoint(tp.getPlace()))
                                .toList()
                );
            }

            System.out.println("[DEBUG] CarDistanceRequest: " + carRequest);
            calculateAndSaveCarRoute(carRequest, trip.getId());
        } else if ("public".equals(normalized)) {
            List<TransDistanceRequest> requests = new ArrayList<>();
            for (int i = 0; i < tripPlaces.size() - 1; i++) {
                PlaceDTO start = tripPlaces.get(i).getPlace();
                PlaceDTO end = tripPlaces.get(i + 1).getPlace();

                TransDistanceRequest req = new TransDistanceRequest();
                req.setStartX(String.valueOf(start.getLongitude()));
                req.setStartY(String.valueOf(start.getLatitude()));
                req.setEndX(String.valueOf(end.getLongitude()));
                req.setEndY(String.valueOf(end.getLatitude()));
              
                requests.add(req);
            }

            TransDistanceBatchRequest batchRequest = new TransDistanceBatchRequest();
            batchRequest.setTripId(trip.getId());
            batchRequest.setRequests(requests);

            System.out.println("[DEBUG] PublicTransport BatchRequest: " + batchRequest);
            calculateAndSavePublicRoute(batchRequest, tripPlaces);
        }
    }

    /**
     * 재계산 진입점: DB에서 조회한 TripPlaceResponse를 직접 사용
     * (프론트엔드에서 온 request가 아닌, DB에 저장된 최신 데이터 사용)
     */
    @Transactional
    public void recalculateRoutesAndSaveV2(TripEntity trip, String transport, List<TripPlaceResponse> tripPlaces) {
        System.out.println("[DEBUG] TripPlaces to recalc (from DB): " + tripPlaces);

        // visitOrder로 정렬
        List<TripPlaceResponse> sortedPlaces = new ArrayList<>(tripPlaces);
        sortedPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        String normalized = transport == null ? "car" : transport.toLowerCase();

        if ("car".equals(normalized)) {
            CarDistanceRequest carRequest = new CarDistanceRequest();
            carRequest.setTripId(trip.getId());

            // 🔥 첫 번째와 마지막 장소를 origin/destination으로 사용
            if (!sortedPlaces.isEmpty()) {
                PlaceDTO firstPlace = sortedPlaces.get(0).getPlace();
                PlaceDTO lastPlace = sortedPlaces.get(sortedPlaces.size() - 1).getPlace();

                carRequest.setOrigin(placeToPoint(firstPlace));
                carRequest.setDestination(placeToPoint(lastPlace));

                // 중간 장소들을 waypoints로 설정
                if (sortedPlaces.size() > 2) {
                    carRequest.setWaypoints(
                            sortedPlaces.subList(1, sortedPlaces.size() - 1)
                                    .stream()
                                    .map(tp -> placeToPoint(tp.getPlace()))
                                    .toList()
                    );
                }
            }

            System.out.println("[DEBUG] CarDistanceRequest: " + carRequest);
            calculateAndSaveCarRoute(carRequest, trip.getId());

        } else if ("public".equals(normalized)) {
            List<TransDistanceRequest> requests = new ArrayList<>();

            for (int i = 0; i < sortedPlaces.size() - 1; i++) {
                PlaceDTO start = sortedPlaces.get(i).getPlace();
                PlaceDTO end = sortedPlaces.get(i + 1).getPlace();

                TransDistanceRequest req = new TransDistanceRequest();
                req.setStartX(String.valueOf(start.getLongitude()));
                req.setStartY(String.valueOf(start.getLatitude()));
                req.setEndX(String.valueOf(end.getLongitude()));
                req.setEndY(String.valueOf(end.getLatitude()));

                requests.add(req);
            }

            TransDistanceBatchRequest batchRequest = new TransDistanceBatchRequest();
            batchRequest.setTripId(trip.getId());
            batchRequest.setRequests(requests);

            System.out.println("[DEBUG] PublicTransport BatchRequest: " + batchRequest);
            calculateAndSavePublicRoute(batchRequest, sortedPlaces);
        }
    }
}
