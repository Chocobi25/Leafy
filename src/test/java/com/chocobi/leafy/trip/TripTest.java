package com.chocobi.leafy.trip;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.Type;
import com.chocobi.leafy.place.repository.PlaceRepository;
import com.chocobi.leafy.trip.dto.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripRequest;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.trip.service.TripService;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class TripTest {
    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserService userService;

    @Test
    void 여행ID_반환() {
        // given
        Long testKakaoId = 12345L;
        String nickname = "초코비";
        String profileImageUrl = "http://example.com/profile.jpg";

        User saveUser = userService.saveOrGetUser(testKakaoId, nickname, profileImageUrl);

        TripRequest tripRequest = new TripRequest();
        tripRequest.setTitle("강릉");
        tripRequest.setStart_date(LocalDate.of(2025, 7, 1));
        tripRequest.setEnd_date(LocalDate.of(2025, 7, 7));
        tripRequest.setUser_id(testKakaoId);

        // when
        Long tripId = tripService.createTrip(tripRequest);

        // then
        assertThat(tripId).isNotNull();
        System.out.println("생성된 Trip ID: " + tripId);
    }

    @Test
    public void 여행장소저장_순서대로저장됨() {
        // given
        Long testKakaoId = 12345L;
        String nickname = "초코비";
        String profileImageUrl = "http://example.com/profile.jpg";
        User user = User.builder()
                .kakaoId(testKakaoId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();

        userService.saveOrGetUser(testKakaoId, nickname, profileImageUrl);

        Trip trip = Trip.builder()
                .user(user)
                .title("테스트 여행")
                .carbon_saved(0)
                .start_date(LocalDate.of(2025, 7, 1))
                .end_date(LocalDate.of(2025, 7, 5))
                .build();

        tripRepository.save(trip);

        Place place1 = placeRepository.save(
                Place.builder()
                        .title("장소1")
                        .address("서울 어딘가")
                        .latitude(37.5)
                        .longitude(127.0)
                        .type(Type.USER)
                        .build()
        );

        Place place2 = placeRepository.save(
                Place.builder()
                        .title("장소2")
                        .address("서울 어딘가2")
                        .latitude(37.6)
                        .longitude(127.1)
                        .type(Type.API)
                        .build()
        );

        TripRequest tripRequest = new TripRequest();

        TripPlaceRequest p1 = new TripPlaceRequest();
        p1.setPlaceId(place1.getId());
        p1.setVisitDate(LocalDate.of(2025, 7, 2));
        p1.setVisitOrder(120);

        TripPlaceRequest p2 = new TripPlaceRequest();
        p2.setPlaceId(place2.getId());
        p2.setVisitDate(LocalDate.of(2025, 7, 2));
        p2.setVisitOrder(200);

        tripRequest.setPlaceList(List.of(p1, p2));

        // when
        tripService.saveTripPlace(tripRequest.getPlaceList(), trip);

        //then
        List<TripPlaceResponse> saved =  tripService.getTripPlaces(trip.getId());
        assertThat(saved).hasSize(2);

        assertThat(saved.get(0).getTripId()).isEqualTo(trip.getId());
        assertThat(saved.get(0).getPlaceId()).isEqualTo(place1.getId());

        assertThat(saved.get(1).getVisitOrder()).isEqualTo(200);
        assertThat(saved.get(1).getPlaceId()).isEqualTo(place2.getId());
    }

}
