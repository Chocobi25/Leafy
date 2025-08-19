package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.constants.Transport;
import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.repository.PlaceRepository;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.dto.TripPlaceListRequest;
import com.chocobi.leafy.trip.dto.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.TripRequest;
import com.chocobi.leafy.trip.entity.TripSegment;
import com.chocobi.leafy.trip.repository.TripSegmentRepository;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TripSegmentServiceIntegrationTest {

    @Autowired
    private TripSegmentService tripSegmentService;

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private TripSegmentRepository tripSegmentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    public void 실제_환경에서_TripSegment_생성_및_Redis_저장_테스트() throws Exception {
        // given
        User user = userService.saveOrGetUser(12345L, "통합 테스트 유저", null);

        Place place1 = Place.builder()
                .title("서울역")
                .latitude(37.5547)
                .longitude(126.9706)
                .address("서울 중구 세종대로")
                .build();
        placeRepository.save(place1);

        Place place2 = Place.builder()
                .title("부산역")
                .latitude(35.1156)
                .longitude(129.0403)
                .address("부산 동구 중앙대로")
                .build();
        placeRepository.save(place2);

        TripRequest tripRequest = new TripRequest();
        tripRequest.setUser_id(user.getKakaoId());
        tripRequest.setTitle("통합 테스트 여행");
        tripRequest.setStart_date(LocalDate.now());
        tripRequest.setEnd_date(LocalDate.now().plusDays(3));

        Long tripId = tripService.createTrip(tripRequest);

        TripPlaceRequest placeReq1 = new TripPlaceRequest();
        placeReq1.setPlaceId(place1.getId());
        placeReq1.setVisitOrder(1);
        placeReq1.setVisitDate(LocalDate.now());

        TripPlaceRequest placeReq2 = new TripPlaceRequest();
        placeReq2.setPlaceId(place2.getId());
        placeReq2.setVisitOrder(2);
        placeReq2.setVisitDate(LocalDate.now());

        TripPlaceListRequest listRequest = new TripPlaceListRequest();
        listRequest.setTripId(tripId);
        listRequest.setPlaceList(Arrays.asList(placeReq1, placeReq2));

        tripService.saveTripPlace(listRequest);

        Section section = new Section();
        section.setDistance(50000); // 50km
        List<Section> sections = Arrays.asList(section);

        // when
        tripSegmentService.completeTempTripSegments(tripId, sections, Transport.CAR);
        tripSegmentService.completeTripSegments(tripId);

        // then
        List<TripSegment> savedSegments = tripSegmentRepository.findAll();
        assertFalse(savedSegments.isEmpty(), "DB에 TripSegment가 저장되어 있어야 함");
        assertEquals(1, savedSegments.size(), "1개의 구간이 저장되어 있어야 함");

        TripSegment savedSegment = savedSegments.get(0);
        assertEquals(Transport.CAR, savedSegment.getTransport());
        assertEquals(50000, savedSegment.getDistance());
        assertEquals(10500, savedSegment.getCarbonEmitted());
        assertEquals(0, savedSegment.getCarbonSaved());
        assertEquals(place1.getId(), savedSegment.getStartPlaceId().getId());
        assertEquals(place2.getId(), savedSegment.getEndPlaceId().getId());
        assertEquals(tripId, savedSegment.getTrip().getId());

        String redisKey = "temp_trip_segments:" + tripId;
        Object deleteData = redisTemplate.opsForValue().get(redisKey);
        assertNull(deleteData, "임시 데이터가 삭제되었어야 함");
    }
}