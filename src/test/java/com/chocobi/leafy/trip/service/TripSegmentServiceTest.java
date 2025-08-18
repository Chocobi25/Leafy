package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.constants.Transport;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripSegment;
import com.chocobi.leafy.trip.repository.TripSegmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripSegmentServiceCarTest {

    @Mock
    private TripSegmentRepository tripSegmentRepository;

    @Mock
    private TripService tripService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private TripSegmentService tripSegmentService;

    @Test
    void 트립세그먼트_정상_생성_테스트() {
        // given
        Long tripId = 1L;
        String transport = Transport.CAR;

        TripPlaceResponse place1 = TripPlaceResponse.builder()
                .placeId(10L)
                .visitOrder(1)
                .build();

        TripPlaceResponse place2 = TripPlaceResponse.builder()
                .placeId(20L)
                .visitOrder(2)
                .build();

        TripPlaceResponse place3 = TripPlaceResponse.builder()
                .placeId(30L)
                .visitOrder(3)
                .build();

        List<TripPlaceResponse> places = Arrays.asList(place1, place2, place3);

        Section section1 = new Section();
        section1.setDistance(1000);

        Section section2 = new Section();
        section2.setDistance(2000);

        List<Section> sections = Arrays.asList(section1, section2);

        // mock 동작 정의
        when(tripService.getTripPlaces(tripId)).thenReturn(places);

        // when
        List<TripSegment> result = tripSegmentService.createTripSegments(tripId, sections, transport);

        // then
        TripSegment segment1 = result.getFirst();
        assertEquals(Transport.CAR, segment1.getTransport());
        assertEquals(1000, segment1.getDistance());
        assertEquals(210.0, segment1.getCarbonEmitted());
        assertEquals(0, segment1.getCarbonSaved());
        assertEquals(10L, segment1.getStartPlaceId().getId());
        assertEquals(20L, segment1.getEndPlaceId().getId());
        assertEquals(1L, segment1.getTrip().getId());

        TripSegment segment2 = result.get(1);
        assertEquals(Transport.CAR, segment2.getTransport());
        assertEquals(2000, segment2.getDistance());
        assertEquals(420.0, segment2.getCarbonEmitted());
        assertEquals(0, segment2.getCarbonSaved());
        assertEquals(20L, segment2.getStartPlaceId().getId());
        assertEquals(30L, segment2.getEndPlaceId().getId());
        assertEquals(1L, segment2.getTrip().getId());

        verify(tripService, times(1)).getTripPlaces(1L);
    }

    @Test
    void Redis_임시_저장_테스트() {
        // given
        TripSegment segment1 = TripSegment.builder()
                .trip(Trip.builder().id(1L).build())
                .distance(1000)
                .transport(Transport.CAR)
                .build();

        TripSegment segment2 = TripSegment.builder()
                .trip(Trip.builder().id(1L).build())
                .distance(2000)
                .transport(Transport.CAR)
                .build();

        List<TripSegment> segments = Arrays.asList(segment1, segment2);

        // RedisTemplate의 opsForValue() Mock 설정
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        Long result = tripSegmentService.saveTempTripSegments(segments);

        // then
        assertEquals(1L, result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set("temp_trip_segments:1", segments);
        verify(redisTemplate).expire("temp_trip_segments:1", 30, java.util.concurrent.TimeUnit.MINUTES);
    }
}