package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripSegmentRedisDto;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripSegment;
import com.chocobi.leafy.trip.repository.TripSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TripSegmentService {
    private final TripSegmentRepository tripSegmentRepository;
    private final TripPlaceService tripPlaceService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * TripSegmentRedisDto를 만들고 Redis에 임시 저장하는 편의 통합 메서드
     *
     * @param tripId
     * @param sections
     * @param transport
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
     * @return
     */
    private List<TripSegmentRedisDto> createTripSegmentRedisDto(Long tripId, List<Section> sections, String transport) {
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        List<TripSegmentRedisDto> tripSegmentDtos = new ArrayList<>();

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        for (int i = 0; i < mutableTripPlaces.size() - 1; i++) {
            TripPlaceResponse startPlace = mutableTripPlaces.get(i);
            TripPlaceResponse endPlace = mutableTripPlaces.get(i + 1);
            double distance = sections.get(i).getDistance();
            int durationInMinutes = Math.max(1, sections.get(i).getDuration() / 60); // 초 -> 분 변환, 최소 1분
            double carbonEmission = sections.get(i).getCarbonEmission();

            TripSegmentRedisDto dto = TripSegmentRedisDto.builder()
                    .tripId(tripId)
                    .startPlaceId(startPlace.getPlaceId())
                    .endPlaceId(endPlace.getPlaceId())
                    .transport(transport)
                    .distance(distance)
                    .duration(durationInMinutes)
                    .carbonEmitted(carbonEmission)
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
        String key = "temp_trip_segments:" + tripId;
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
     * @return
     */
    public List<TripSegment> createTripSegments(Long tripId, List<Section> sections, String transport) {
        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        List<TripSegment> tripSegments = new ArrayList<>();

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        for (int i = 0; i < mutableTripPlaces.size() - 1; i++) {
            TripPlaceResponse startPlace = mutableTripPlaces.get(i);
            TripPlaceResponse endPlace = mutableTripPlaces.get(i + 1);
            double distance = sections.get(i).getDistance();
            int durationInMinutes = Math.max(1, sections.get(i).getDuration() / 60); // 초 -> 분 변환, 최소 1분
            double carbonEmission = sections.get(i).getCarbonEmission();

            TripSegment tripSegment = TripSegment.builder()
                    .tripId(Trip.builder().id(tripId).build())
                    .startPlaceId(Place.builder().id(startPlace.getPlaceId()).build())
                    .endPlaceId(Place.builder().id(endPlace.getPlaceId()).build())
                    .transport(transport)
                    .distance(distance)
                    .duration(durationInMinutes)
                    .carbonEmitted(carbonEmission)
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
        List<TripSegmentRedisDto> tripSegmentDtos = getTempTripSegments(tripId); // 임시 TripSegmentRedisDto를 불러와서
        
        // 최종 선택된 교통수단으로 transport 값 업데이트
        List<TripSegment> tripSegments = tripSegmentDtos.stream()
                .map(dto -> {
                    // DTO의 transport를 최종 선택된 값으로 업데이트
                    dto.setTransport(transport);
                    return dto.toEntity();
                })
                .toList();
        
        saveTripSegments(tripSegments); // DB에 저장
        deleteTempTripSegments(tripId); // 임시 저장한 TripSegments를 삭제
    }

    /**
     * Redis에서 임시 TripSegmentRedisDto 가져오기
     *
     * @param tripId
     * @return
     */
    public List<TripSegmentRedisDto> getTempTripSegments(Long tripId) {
        String key = "temp_trip_segments:" + tripId;
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            throw new IllegalArgumentException("임시 저장된 TripSegments가 없습니다. tripId: " + tripId);
        }

        return (List<TripSegmentRedisDto>) result;
    }

    /**
     * Redis에서 TripSegmentRedisDto 삭제
     *
     * @param tripId
     */
    public void deleteTempTripSegments(Long tripId) {
        String key = "temp_trip_segments:" + tripId;
        redisTemplate.delete(key);
    }

    /**
     * Redis에 저장된 TripSegment들의 총 시간과 탄소배출량 계산
     *
     * @param tripId
     * @return Map with totalDuration and totalCarbonEmission
     */
    public Map<String, Object> getTotalTimeAndCarbon(Long tripId) {
        List<TripSegmentRedisDto> segments = getTempTripSegments(tripId);
        
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
}
