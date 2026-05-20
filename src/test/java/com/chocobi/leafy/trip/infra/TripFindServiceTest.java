package com.chocobi.leafy.trip.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionLevel;
import com.chocobi.leafy.global.entity.RegionRepository;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.repository.TripRepository;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.entity.enums.Provider;
import com.chocobi.leafy.user.infra.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TripFindService.class)
@ActiveProfiles("test")
class TripFindServiceTest {

    @Autowired
    private TripFindService tripFindService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    @DisplayName("존재하는 여행을 조회한다")
    void findTrip() {
        TripEntity trip = saveTrip("부산 여행", saveUser("user-1"));

        TripEntity result = tripFindService.findTrip(trip.getId());

        assertThat(result.getId()).isEqualTo(trip.getId());
        assertThat(result.getTitle()).isEqualTo("부산 여행");
    }

    @Test
    @DisplayName("존재하지 않는 여행이면 예외가 발생한다")
    void findTrip_NotFound() {
        assertThatThrownBy(() -> tripFindService.findTrip(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_NOT_FOUND);
    }

    @Test
    @DisplayName("내 여행을 조회한다")
    void findOwnedTrip() {
        UserEntity user = saveUser("user-1");
        TripEntity trip = saveTrip("내 여행", user);

        TripEntity result = tripFindService.findOwnedTrip(trip.getId(), user.getId());

        assertThat(result.getId()).isEqualTo(trip.getId());
    }

    @Test
    @DisplayName("다른 사용자의 여행이면 접근 거부 예외가 발생한다")
    void findOwnedTrip_AccessDenied() {
        TripEntity trip = saveTrip("다른 사용자 여행", saveUser("owner"));
        UserEntity otherUser = saveUser("other");

        assertThatThrownBy(() -> tripFindService.findOwnedTrip(trip.getId(), otherUser.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_ACCESS_DENIED);
    }

    @Test
    @DisplayName("사용자별 여행 목록을 최신 생성순으로 조회한다")
    void findTripsByUserId() {
        UserEntity user = saveUser("user-1");
        UserEntity otherUser = saveUser("other");
        saveTrip("첫 번째 여행", user);
        saveTrip("다른 사용자 여행", otherUser);
        saveTrip("두 번째 여행", user);

        List<TripEntity> result = tripFindService.findTripsByUserId(user.getId());

        assertThat(result).extracting(TripEntity::getTitle)
                .containsExactly("두 번째 여행", "첫 번째 여행");
    }

    private TripEntity saveTrip(String title, UserEntity user) {
        RegionEntity departure = regionRepository.saveAndFlush(new RegionEntity(title + " 출발", null, RegionLevel.SIDO));
        RegionEntity arrival = regionRepository.saveAndFlush(new RegionEntity(title + " 도착", null, RegionLevel.SIDO));

        return tripRepository.saveAndFlush(TripEntity.builder()
                .user(user)
                .title(title)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 3))
                .departure(departure)
                .arrival(arrival)
                .build());
    }

    private UserEntity saveUser(String providerId) {
        return userRepository.saveAndFlush(UserEntity.builder()
                .nickname("테스터" + providerId)
                .profileImageUrl("https://example.com/profile.png")
                .provider(Provider.KAKAO)
                .providerId(providerId)
                .build());
    }
}
