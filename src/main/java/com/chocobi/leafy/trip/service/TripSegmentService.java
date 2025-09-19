package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.constants.CarbonEmissionConst;
import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.TransDistanceBatchRequest;
import com.chocobi.leafy.distance.dto.CarDistanceResponse;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.service.DistanceUtils;
import com.chocobi.leafy.distance.service.TransDistanceService;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripSegmentDTO;
import com.chocobi.leafy.trip.dto.TripSegmentRedisDto;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripSegment;
import com.chocobi.leafy.trip.repository.TripSegmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripSegmentService {
    private final TripSegmentRepository tripSegmentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CarDistanceService carDistanceService;
    private final TransDistanceService transDistanceService;
    private final TripPlaceService tripPlaceService;

    /**
     * TripSegmentRedisDto를 만들고 Redis에 임시 저장하는 편의 통합 메서드
     *
     * @param tripId
     * @param sections
     * @param transport
     * @param tripPlaces ⭐️ tripPlaces를 인자로 받습니다.
     */
    public void completeTempTripSegments(Long tripId, List<Section> sections, String transport) {
        List<TripSegmentRedisDto> tripSegmentDtos = createTripSegmentRedisDto(tripId, sections, transport);
        saveTempTripSegments(tripSegmentDtos);
    }

    /**
     * TripSegmentRedisDto 생성 메서드
     *
     * @param tripId
     * @param sections
     * @param transport
     * @param tripPlaces ⭐️ tripPlaces를 인자로 받습니다.
     * @return
     */
    private List<TripSegmentRedisDto> createTripSegmentRedisDto(Long tripId, List<Section> sections, String transport, List<TripPlaceResponse> tripPlaces) {
        List<TripSegmentRedisDto> tripSegmentDtos = new ArrayList<>();

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        for (int i = 0; i < mutableTripPlaces.size() - 1; i++) {
            TripPlaceResponse startPlace = mutableTripPlaces.get(i);
            TripPlaceResponse endPlace = mutableTripPlaces.get(i + 1);
            double distance = sections.get(i).getDistance();
            int durationInMinutes = Math.max(1, sections.get(i).getDuration() / 60); // 초 -> 분 변환, 최소 1분
            double carbonEmission = sections.get(i).getCarbonEmission();
            double maxCarbonEmission = sections.get(i).getMaxCarbonEmission();

            TripSegmentRedisDto dto = TripSegmentRedisDto.builder()
                    .tripId(tripId)
                    .startPlaceId(startPlace.getPlace().getId())
                    .endPlaceId(endPlace.getPlace().getId())
                    .transport(transport)
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
     *
     * @param tripSegmentDtos
     * @return
     */
    public Long saveTempTripSegments(List<TripSegmentRedisDto> tripSegmentDtos) {

        if (tripSegmentDtos == null || tripSegmentDtos.isEmpty()) {
            throw new IllegalArgumentException("TripSegmentDtos가 비어있습니다.");
        }

        Long tripId = tripSegmentDtos.getFirst().getTripId();
        String transport = tripSegmentDtos.getFirst().getTransport();
        String key = "temp_trip_segments:" + tripId + ":" + transport;
        redisTemplate.opsForValue().set(key, tripSegmentDtos);
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);

        return tripId;
    }

    /**
     * 트립 세그먼트 만들기
     *
     * @param tripId    트립 아이디
     * @param sections  구간별 거리 정보 담김
     * @param transport 교통 수단
     * @param tripPlaces ⭐️ tripPlaces를 인자로 받습니다.
     * @return
     */
    public List<TripSegment> createTripSegments(Long tripId, List<Section> sections, String transport, List<TripPlaceResponse> tripPlaces) {
        List<TripSegment> tripSegments = new ArrayList<>();

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        for (int i = 0; i < mutableTripPlaces.size() - 1; i++) {
            TripPlaceResponse startPlace = mutableTripPlaces.get(i);
            TripPlaceResponse endPlace = mutableTripPlaces.get(i + 1);
            double distance = sections.get(i).getDistance();
            int durationInMinutes = Math.max(1, sections.get(i).getDuration() / 60); // 초 -> 분 변환, 최소 1분
            double carbonEmission = sections.get(i).getCarbonEmission();
            double maxCarbonEmission = sections.get(i).getMaxCarbonEmission();

            TripSegment tripSegment = TripSegment.builder()
                    .tripId(Trip.builder().id(tripId).build())
                    .startPlaceId(Place.builder().id(startPlace.getPlace().getId()).build())
                    .endPlaceId(Place.builder().id(endPlace.getPlace().getId()).build())
                    .transport(transport)
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
     * 임시 TripSegments를 DB에 저장하고 Redis에서 삭제하는 편의 통합 메서드
     *
     * @param tripId
     * @param transport 최종 선택된 교통수단
     */
    public void completeTripSegments(Long tripId, String transport) {
        List<TripSegmentRedisDto> tripSegmentDtos = getTempTripSegments(tripId, transport); // 임시 TripSegmentRedisDto를 불러와서

        String otherTransport = transport.equals("car") ? "public" : "car";
        List<TripSegmentRedisDto> otherTripSegmentDtos = null;

        try {
            otherTripSegmentDtos = getTempTripSegments(tripId, otherTransport);
        } catch (IllegalArgumentException e) {
        }

        // 최종 선택된 교통수단으로 transport 값 업데이트 및 maxCarbonEmission 설정
        List<TripSegment> tripSegments = new ArrayList<>();
        for (int i = 0; i < tripSegmentDtos.size(); i++) {
            TripSegmentRedisDto dto = tripSegmentDtos.get(i);
            dto.setTransport(transport);

            // maxCarbonEmission 설정: 현재 교통수단과 다른 교통수단의 maxCarbonEmission 중 더 큰 값
            double maxCarbon = dto.getMaxCarbonEmission();
            if (otherTripSegmentDtos != null && i < otherTripSegmentDtos.size()) {
                double otherMaxCarbon = otherTripSegmentDtos.get(i).getMaxCarbonEmission();
                maxCarbon = Math.max(maxCarbon, otherMaxCarbon);
            }
            dto.setMaxCarbonEmission(maxCarbon);

            tripSegments.add(dto.toEntity());
        }

        saveTripSegments(tripSegments); // DB에 저장
        deleteTempTripSegments(tripId, transport); // 임시 저장한 TripSegments를 삭제
        deleteTempTripSegments(tripId, otherTransport);
    }

    /**
     * Redis에서 임시 TripSegmentRedisDto 가져오기
     *
     * @param tripId
     * @return
     */
    public List<TripSegmentRedisDto> getTempTripSegments(Long tripId, String transport) {
        String key = "temp_trip_segments:" + tripId + ":" + transport;
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            throw new IllegalArgumentException("임시 저장된 TripSegments가 없습니다. tripId: " + tripId + ", transport: " + transport);
        }

        return (List<TripSegmentRedisDto>) result;
    }

    /**
     * Redis에서 TripSegmentRedisDto 삭제
     *
     * @param tripId
     */
    public void deleteTempTripSegments(Long tripId, String transport) {
        String key = "temp_trip_segments:" + tripId + ":" + transport;
        redisTemplate.delete(key);
    }

    /**
     * Redis에 저장된 TripSegment들의 총 시간과 탄소배출량 계산
     *
     * @param tripId
     * @param transport
     * @return Map with totalDuration and totalCarbonEmission
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
     *
     * @param tripSegments
     */
    public void saveTripSegments(List<TripSegment> tripSegments) {
        tripSegmentRepository.saveAll(tripSegments);
    }

    /**
     * 자동차 경로 계산 및 Redis 저장 통합 메서드
     *
     * @param request
     * @param tripId
     * @param tripPlaces ⭐️ tripPlaces를 인자로 받습니다.
     * @return
     */
    @Transactional
    public DistanceResponse calculateAndSaveCarRoute(CarDistanceRequest request, Long tripId) {
        // ⭐️ 서비스 내부에서 tripPlaces를 가져옵니다.
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

        // Redis에 저장
        completeTempTripSegments(tripId, sections, "car", tripPlaces);

        return carResponse.getDistanceResponse();
    }

    /**
     * 대중교통 경로 계산 및 Redis 저장 통합 메서드
     *
     * @param batchRequest
     * @param tripPlaces ⭐️ tripPlaces를 인자로 받습니다.
     * @return
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

        completeTempTripSegments(batchRequest.getTripId(), sections, "public");

        return results;
    }

    @Transactional
    public List<TripSegmentDTO> getTripSegments(Long tripId) {
        return tripSegmentRepository.findByTripId_Id(tripId).stream()
                .map(TripSegmentDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteTripSegments(Trip trip) {
        tripSegmentRepository.deleteAllByTripId(trip);
    }
}
