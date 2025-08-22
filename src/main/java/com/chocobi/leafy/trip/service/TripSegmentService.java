package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripSegment;
import com.chocobi.leafy.trip.repository.TripSegmentRepository;
import com.chocobi.leafy.util.CarbonCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripSegmentService {
    private final TripSegmentRepository tripSegmentRepository;
    private final TripService tripService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * TripSegments를 만들고 임시 저장하는 편의 통합 메서드
     * @param tripId
     * @param sections
     * @param transport
     */
    public void completeTempTripSegments(Long tripId, List<Section> sections, String transport) {
        List<TripSegment> tripSegments = createTripSegments(tripId, sections, transport);
        saveTempTripSegments(tripSegments);
    }

    /**
     * 트립 세그먼트 만들기
     * @param tripId 트립 아이디
     * @param sections 구간별 거리 정보 담김
     * @param transport 교통 수단
     * @return
     */
    public List<TripSegment> createTripSegments(Long tripId, List<Section> sections, String transport) {
        List<TripPlaceResponse> tripPlaces = tripService.getTripPlaces(tripId);
        List<TripSegment> tripSegments = new ArrayList<>();

        List<TripPlaceResponse> mutableTripPlaces = new ArrayList<>(tripPlaces);
        mutableTripPlaces.sort(Comparator.comparing(TripPlaceResponse::getVisitOrder));

        for (int i = 0; i < sections.size(); i++) {
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
     * Redis에 TripSegments 임시 저장
     * @param tripSegments
     * @return
     */
    public Long saveTempTripSegments(List<TripSegment> tripSegments) {

        if (tripSegments == null || tripSegments.isEmpty()) {
            throw new IllegalArgumentException("TripSegments가 비어있습니다.");
        }

        Long tripId = tripSegments.getFirst().getTrip().getId();
        String key = "temp_trip_segments:" + tripId;
        redisTemplate.opsForValue().set(key, tripSegments);
        redisTemplate.expire(key, 30, java.util.concurrent.TimeUnit.MINUTES); // 만료시간 30분

        return tripId;
    }

    /**
     * 임시 TripSegments를 DB에 저장하고 Redis에서 삭제하는 편의 통합 메서드
     * @param tripId
     */
    public void completeTripSegments(Long tripId) {
        List<TripSegment> tripSegments = getTempTripSegments(tripId); // 임시 TripSegments를 불러와서
        saveTripSegments(tripSegments); // DB에 저장
        deleteTempTripSegments(tripId); // 임시 저장한 TripSegments를 삭제
    }

    /**
     * Redis에서 임시 TripSegments 가져오기
     * @param tripId
     * @return
     */
    public List<TripSegment> getTempTripSegments(Long tripId) {
        String key = "temp_trip_segments:" + tripId;
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            throw new IllegalArgumentException("임시 저장된 TripSegments가 없습니다. tripId: " + tripId);
        }

        return (List<TripSegment>) result;
    }

    /**
     * Redis에서 TripSegments 삭제
     * @param tripId
     */
    public void deleteTempTripSegments(Long tripId) {
        String key = "temp_trip_segments:" + tripId;
        redisTemplate.delete(key);
    }

    /**
     * DB에 TripSegment 저장
     * @param tripSegments
     */
    public void saveTripSegments(List<TripSegment> tripSegments) {
        tripSegmentRepository.saveAll(tripSegments);
    }
}
