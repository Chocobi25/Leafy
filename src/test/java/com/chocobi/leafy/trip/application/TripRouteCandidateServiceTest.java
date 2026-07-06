package com.chocobi.leafy.trip.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;

import com.chocobi.leafy.distance.dto.Section;
import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionLevel;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripRouteOptionFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.trip.vo.TripTransport;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.entity.enums.Provider;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TripRouteCandidateServiceTest {

    @InjectMocks
    private TripRouteCandidateService tripRouteCandidateService;

    @Mock
    private TripFindService tripFindService;

    @Mock
    private TripRouteOptionCommandService tripRouteOptionCommandService;

    @Mock
    private TripRouteOptionFindService tripRouteOptionFindService;

    @Mock
    private TripPlaceService tripPlaceService;

    @Test
    @DisplayName("경로 후보와 구간을 RouteOption 중심으로 저장한다")
    void saveRouteCandidate() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity firstTripPlace = tripPlaceFixture(10L, trip, placeFixture(100L, "첫 장소"), 0, 0);
        TripPlaceEntity secondTripPlace = tripPlaceFixture(20L, trip, placeFixture(200L, "둘째 장소"), 0, 1);
        List<TripPlaceResponse> tripPlaces = List.of(
                TripPlaceResponse.from(secondTripPlace),
                TripPlaceResponse.from(firstTripPlace)
        );
        List<Section> sections = List.of(section(120, 1500, 10.0));

        given(tripFindService.findTrip(1L)).willReturn(trip);
        given(tripRouteOptionFindService.findTripRouteOptionCandidate(1L, TripTransport.CAR)).willReturn(Optional.empty());
        given(tripPlaceService.getTripPlaceById(10L)).willReturn(firstTripPlace);
        given(tripPlaceService.getTripPlaceById(20L)).willReturn(secondTripPlace);

        tripRouteCandidateService.saveRouteCandidate(1L, sections, TripTransport.CAR, tripPlaces);

        ArgumentCaptor<TripRouteOptionEntity> routeOptionCaptor = ArgumentCaptor.forClass(TripRouteOptionEntity.class);
        then(tripRouteOptionCommandService).should().save(routeOptionCaptor.capture());

        TripRouteOptionEntity savedRouteOption = routeOptionCaptor.getValue();
        assertThat(savedRouteOption.getTrip()).isEqualTo(trip);
        assertThat(savedRouteOption.getTransport()).isEqualTo(TripTransport.CAR);
        assertThat(savedRouteOption.getTotalDistance()).isEqualTo(1500.0);
        assertThat(savedRouteOption.getTotalDuration()).isEqualTo(2);
        assertThat(savedRouteOption.getTotalCarbonEmission()).isEqualTo(10.0);
        assertThat(savedRouteOption.isConfirmed()).isFalse();

        assertThat(savedRouteOption.getSegments()).hasSize(1);
        TripSegmentEntity segment = savedRouteOption.getSegments().getFirst();
        assertThat(segment.getRouteOption()).isEqualTo(savedRouteOption);
        assertThat(segment.getStartTripPlace()).isEqualTo(firstTripPlace);
        assertThat(segment.getEndTripPlace()).isEqualTo(secondTripPlace);
        assertThat(segment.getDistance()).isEqualTo(1500.0);
        assertThat(segment.getDuration()).isEqualTo(2);
        assertThat(segment.getCarbonEmission()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("같은 교통수단 후보가 있으면 기존 후보를 삭제하고 새 후보를 저장한다")
    void saveRouteCandidateReplacesExistingCandidate() {
        TripEntity trip = tripFixture(1L);
        TripRouteOptionEntity existingRouteOption = routeOptionFixture(trip, TripTransport.CAR, false);
        TripPlaceEntity firstTripPlace = tripPlaceFixture(10L, trip, placeFixture(100L, "첫 장소"), 0, 0);
        TripPlaceEntity secondTripPlace = tripPlaceFixture(20L, trip, placeFixture(200L, "둘째 장소"), 0, 1);

        given(tripFindService.findTrip(1L)).willReturn(trip);
        given(tripRouteOptionFindService.findTripRouteOptionCandidate(1L, TripTransport.CAR))
                .willReturn(Optional.of(existingRouteOption));
        given(tripPlaceService.getTripPlaceById(10L)).willReturn(firstTripPlace);
        given(tripPlaceService.getTripPlaceById(20L)).willReturn(secondTripPlace);

        tripRouteCandidateService.saveRouteCandidate(
                1L,
                List.of(section(60, 3000, 20.0)),
                TripTransport.CAR,
                List.of(TripPlaceResponse.from(firstTripPlace), TripPlaceResponse.from(secondTripPlace))
        );

        then(tripRouteOptionCommandService).should().delete(existingRouteOption);
        then(tripRouteOptionCommandService).should().save(org.mockito.ArgumentMatchers.any(TripRouteOptionEntity.class));
    }

    @Test
    @DisplayName("여행 장소가 2개 미만이면 경로 후보를 저장할 수 없다")
    void saveRouteCandidateWithInsufficientTripPlaces() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity tripPlace = tripPlaceFixture(10L, trip, placeFixture(100L, "첫 장소"), 0, 0);

        assertThatThrownBy(() -> tripRouteCandidateService.saveRouteCandidate(
                1L,
                List.of(section(60, 1000, 5.0)),
                TripTransport.CAR,
                List.of(TripPlaceResponse.from(tripPlace))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 장소는 2개 이상 필요합니다.");

        then(tripFindService).should(never()).findTrip(1L);
        then(tripRouteOptionCommandService).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("경로 구간이 없으면 경로 후보를 저장할 수 없다")
    void saveRouteCandidateWithEmptySections() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity firstTripPlace = tripPlaceFixture(10L, trip, placeFixture(100L, "첫 장소"), 0, 0);
        TripPlaceEntity secondTripPlace = tripPlaceFixture(20L, trip, placeFixture(200L, "둘째 장소"), 0, 1);

        assertThatThrownBy(() -> tripRouteCandidateService.saveRouteCandidate(
                1L,
                List.of(),
                TripTransport.CAR,
                List.of(TripPlaceResponse.from(firstTripPlace), TripPlaceResponse.from(secondTripPlace))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장할 여행 경로 구간이 없습니다.");

        then(tripFindService).should(never()).findTrip(1L);
        then(tripRouteOptionCommandService).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("선택한 경로 후보만 확정한다")
    void completeRouteCandidate() {
        TripEntity trip = tripFixture(1L);
        trip.markRouteStale();
        TripRouteOptionEntity carOption = routeOptionFixture(trip, TripTransport.CAR, false);
        TripRouteOptionEntity publicOption = routeOptionFixture(trip, TripTransport.PUBLIC, true);

        given(tripFindService.findTrip(1L)).willReturn(trip);
        given(tripRouteOptionFindService.findTripRouteOption(1L, "car")).willReturn(carOption);
        given(tripRouteOptionFindService.findTripRouteOptions(1L)).willReturn(List.of(carOption, publicOption));
        willAnswer(invocation -> {
            TripRouteOptionEntity selectedRouteOption = invocation.getArgument(0);
            List<TripRouteOptionEntity> routeOptions = invocation.getArgument(1);
            routeOptions.forEach(TripRouteOptionEntity::unconfirm);
            selectedRouteOption.confirm();
            return null;
        }).given(tripRouteOptionCommandService).confirmOnly(carOption, List.of(carOption, publicOption));

        tripRouteCandidateService.completeRouteCandidate(1L, "car");

        assertThat(carOption.isConfirmed()).isTrue();
        assertThat(publicOption.isConfirmed()).isFalse();
        assertThat(trip.isRouteStale()).isFalse();
    }

    @Test
    @DisplayName("교통수단이 없으면 경로 후보를 확정할 수 없다")
    void completeRouteCandidateWithoutTransport() {
        assertThatThrownBy(() -> tripRouteCandidateService.completeRouteCandidate(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("transport가 필요합니다.");

        then(tripFindService).should(never()).findTrip(1L);
        then(tripRouteOptionCommandService).should(never()).confirmOnly(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("확정할 경로 후보가 없으면 예외를 전파한다")
    void completeRouteCandidateWithoutCandidate() {
        TripEntity trip = tripFixture(1L);
        given(tripFindService.findTrip(1L)).willReturn(trip);
        given(tripRouteOptionFindService.findTripRouteOption(1L, "car"))
                .willThrow(new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));

        assertThatThrownBy(() -> tripRouteCandidateService.completeRouteCandidate(1L, "car"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_ROUTE_OPTION_NOT_FOUND);

        then(tripRouteOptionCommandService).should(never()).confirmOnly(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("경로 후보의 총 시간과 탄소 배출량을 조회한다")
    void getTotalTimeAndCarbon() {
        TripRouteOptionEntity routeOption = TripRouteOptionEntity.builder()
                .trip(tripFixture(1L))
                .transport(TripTransport.PUBLIC)
                .totalDistance(12.5)
                .totalDuration(45)
                .totalCarbonEmission(7.2)
                .confirmed(false)
                .build();
        given(tripRouteOptionFindService.findTripRouteOption(1L, "public")).willReturn(routeOption);

        Map<String, Object> result = tripRouteCandidateService.getTotalTimeAndCarbon(1L, "public");

        assertThat(result)
                .containsEntry("totalDuration", 45)
                .containsEntry("totalCarbonEmission", 7.2);
    }

    private Section section(int duration, int distance, double carbonEmission) {
        Section section = new Section();
        section.setDuration(duration);
        section.setDistance(distance);
        section.setCarbonEmission(carbonEmission);
        return section;
    }

    private TripRouteOptionEntity routeOptionFixture(TripEntity trip, TripTransport transport, boolean confirmed) {
        return TripRouteOptionEntity.builder()
                .trip(trip)
                .transport(transport)
                .totalDistance(10.0)
                .totalDuration(30)
                .totalCarbonEmission(5.0)
                .confirmed(confirmed)
                .build();
    }

    private TripPlaceEntity tripPlaceFixture(
            Long tripPlaceId,
            TripEntity trip,
            PlaceEntity place,
            int dayIndex,
            int visitOrder
    ) {
        TripPlaceEntity tripPlace = TripPlaceEntity.builder()
                .trip(trip)
                .place(place)
                .dayIndex(dayIndex)
                .visitOrder(visitOrder)
                .memo("메모")
                .build();
        ReflectionTestUtils.setField(tripPlace, "id", tripPlaceId);
        return tripPlace;
    }

    private TripEntity tripFixture(Long tripId) {
        TripEntity trip = TripEntity.builder()
                .user(userFixture(1L))
                .title("부산 여행")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 3))
                .departure(regionFixture(10L, "서울"))
                .arrival(regionFixture(20L, "부산"))
                .build();
        ReflectionTestUtils.setField(trip, "id", tripId);
        return trip;
    }

    private ExternalPlaceEntity placeFixture(Long placeId, String title) {
        ExternalPlaceEntity place = ExternalPlaceEntity.builder()
                .title(title)
                .address(title + " 주소")
                .latitude(37.5)
                .longitude(127.1)
                .copyright("테스트")
                .description(title + " 설명")
                .tel("051-000-0000")
                .url("https://example.com/" + placeId)
                .build();
        ReflectionTestUtils.setField(place, "id", placeId);
        return place;
    }

    private UserEntity userFixture(Long userId) {
        UserEntity user = UserEntity.builder()
                .nickname("테스터")
                .profileImageUrl("https://example.com/profile.png")
                .provider(Provider.KAKAO)
                .providerId("provider-" + userId)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private RegionEntity regionFixture(Long regionId, String name) {
        RegionEntity region = RegionEntity.builder()
                .code(name)
                .name(name)
                .fullName(name)
                .level(RegionLevel.SIDO)
                .build();
        ReflectionTestUtils.setField(region, "id", regionId);
        return region;
    }
}
