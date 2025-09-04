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
import java.util.List;
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
        System.out.println("=== completeTempTripSegments 호출됨 ===");
        System.out.println("tripId: " + tripId);
        System.out.println("transport: " + transport);
        System.out.println("받은 sections 개수: " + sections.size());
        
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            System.out.println("Section[" + i + "] - distance: " + section.getDistance() + 
                              "m, carbonEmission: " + section.getCarbonEmission() + "kg");
        }
        
        List<TripSegmentRedisDto> tripSegmentDtos = createTripSegmentRedisDto(tripId, sections, transport);
        System.out.println("생성된 TripSegmentRedisDto 개수: " + tripSegmentDtos.size());
        
        for (int i = 0; i < tripSegmentDtos.size(); i++) {
            TripSegmentRedisDto dto = tripSegmentDtos.get(i);
            System.out.println("TripSegmentRedisDto[" + i + "] - startPlaceId: " + dto.getStartPlaceId() + 
                              ", endPlaceId: " + dto.getEndPlaceId() + 
                              ", distance: " + dto.getDistance() + 
                              ", carbonEmitted: " + dto.getCarbonEmitted());
        }
        
        saveTempTripSegments(tripSegmentDtos);
        System.out.println("=== Redis 저장 완료 ===");
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
            double carbonEmission = sections.get(i).getCarbonEmission();

            TripSegmentRedisDto dto = TripSegmentRedisDto.builder()
                    .tripId(tripId)
                    .startPlaceId(startPlace.getPlaceId())
                    .endPlaceId(endPlace.getPlaceId())
                    .transport(transport)
                    .distance(distance)
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
            double carbonEmission = sections.get(i).getCarbonEmission();

            TripSegment tripSegment = TripSegment.builder()
                    .trip(Trip.builder().id(tripId).build())
                    .startPlaceId(Place.builder().id(startPlace.getPlaceId()).build())
                    .endPlaceId(Place.builder().id(endPlace.getPlaceId()).build())
                    .transport(transport)
                    .distance(distance)
                    .carbonEmitted(carbonEmission)
                    .carbonSaved(0)
                    .build();
            tripSegments.add(tripSegment);
        }

        return tripSegments;
    }

    /**
     * 임시 TripSegments를 DB에 저장하고 Redis에서 삭제하는 편의 통합 메서드
     *
     * @param tripId
     */
    public void completeTripSegments(Long tripId) {
        List<TripSegmentRedisDto> tripSegmentDtos = getTempTripSegments(tripId); // 임시 TripSegmentRedisDto를 불러와서
        List<TripSegment> tripSegments = tripSegmentDtos.stream()
                .map(TripSegmentRedisDto::toEntity)
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
     * DB에 TripSegment 저장
     *
     * @param tripSegments
     */
    public void saveTripSegments(List<TripSegment> tripSegments) {
        tripSegmentRepository.saveAll(tripSegments);
    }
}
