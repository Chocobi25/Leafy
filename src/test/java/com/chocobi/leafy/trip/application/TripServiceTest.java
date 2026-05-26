package com.chocobi.leafy.trip.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionLevel;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.global.service.RegionFindService;
import com.chocobi.leafy.external.kakao.dto.GeocodeResponse.Address;
import com.chocobi.leafy.trip.client.TransCoordDTO;
import com.chocobi.leafy.trip.client.TransCoordResponse;
import com.chocobi.leafy.trip.client.TransDocument;
import com.chocobi.leafy.trip.client.TranscodeClient;
import com.chocobi.leafy.trip.dto.request.CreateTripRequest;
import com.chocobi.leafy.trip.dto.request.TripUpdateRequest;
import com.chocobi.leafy.trip.dto.response.TripDetailResponse;
import com.chocobi.leafy.trip.dto.response.TripListResponse;
import com.chocobi.leafy.trip.dto.response.TripSaveResponse;
import com.chocobi.leafy.trip.infra.TripCommandService;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripPlaceCommandService;
import com.chocobi.leafy.trip.infra.TripPlaceFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.entity.enums.Provider;
import com.chocobi.leafy.user.infra.service.UserService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @InjectMocks
    private TripService tripService;

    @Mock
    private TripFindService tripFindService;

    @Mock
    private TripCommandService tripCommandService;

    @Mock
    private UserService userService;

    @Mock
    private TripPlaceFindService tripPlaceFindService;

    @Mock
    private TripPlaceCommandService tripPlaceCommandService;

    @Mock
    private TripSegmentService tripSegmentService;

    @Mock
    private TranscodeClient transcodeClient;

    @Mock
    private RegionFindService regionFindService;

    @Test
    @DisplayName("여행을 생성한다")
    void createTrip() {
        UserEntity user = userFixture(1L);
        RegionEntity seoul = regionFixture(10L, "서울");
        RegionEntity busan = regionFixture(20L, "부산");
        CreateTripRequest request = new CreateTripRequest(
                "부산 여행",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                "서울",
                "부산"
        );

        given(regionFindService.findRegion("서울")).willReturn(seoul);
        given(regionFindService.findRegion("부산")).willReturn(busan);
        given(userService.findById(1L)).willReturn(user);
        given(tripCommandService.save(any(TripEntity.class))).willAnswer(invocation -> {
            TripEntity trip = invocation.getArgument(0);
            ReflectionTestUtils.setField(trip, "id", 100L);
            return trip;
        });

        TripSaveResponse result = tripService.createTrip(request, 1L);

        assertThat(result.getTripId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(TripStatus.CREATING);

        ArgumentCaptor<TripEntity> tripCaptor = ArgumentCaptor.forClass(TripEntity.class);
        then(tripCommandService).should().save(tripCaptor.capture());
        TripEntity savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getUser()).isEqualTo(user);
        assertThat(savedTrip.getDeparture()).isEqualTo(seoul);
        assertThat(savedTrip.getArrival()).isEqualTo(busan);
        assertThat(savedTrip.getTitle()).isEqualTo("부산 여행");
    }

    @Test
    @DisplayName("사용자의 여행 목록을 응답 DTO로 변환한다")
    void getTrips() {
        given(tripFindService.findTripsByUserId(1L))
                .willReturn(List.of(tripFixture(10L, userFixture(1L), "부산 여행")));

        List<TripListResponse> result = tripService.getTrips(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTripId()).isEqualTo(10L);
        assertThat(result.getFirst().getTitle()).isEqualTo("부산 여행");
    }

    @Test
    @DisplayName("여행 상세 정보를 수정한다")
    void updateTripInfo() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "기존 여행");
        TripUpdateRequest request = new TripUpdateRequest(
                "수정된 여행",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2)
        );

        given(tripFindService.findOwnedTripDetail(10L, 1L)).willReturn(trip);
        given(tripSegmentService.getTripSegments(10L)).willReturn(List.of());
        given(tripPlaceFindService.findOrderedTripPlaces(10L)).willReturn(List.of());

        TripDetailResponse result = tripService.updateTripInfo(10L, request, 1L);

        assertThat(result.getTitle()).isEqualTo("수정된 여행");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 7, 2));
        then(tripFindService).should().findOwnedTripDetail(10L, 1L);
    }

    @Test
    @DisplayName("여행 삭제 시 소유권을 검증하고 연관 데이터를 먼저 삭제한다")
    void deleteTrip() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "삭제할 여행");
        given(tripFindService.findOwnedTrip(10L, 1L)).willReturn(trip);

        tripService.deleteTrip(10L, 1L);

        then(tripFindService).should().findOwnedTrip(10L, 1L);
        then(tripSegmentService).should().deleteTripSegments(trip);
        then(tripPlaceCommandService).should().deleteAll(trip);
        then(tripCommandService).should().delete(trip);
    }

    @Test
    @DisplayName("진행 중인 여행을 도착 지역에서 인증한다")
    void certifyTrip() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "부산 여행");
        trip.editStatus(TripStatus.IN_PROGRESS);
        RegionEntity busan = regionFixture(20L, "부산");
        ReflectionTestUtils.setField(trip, "arrival", busan);

        TransCoordDTO coord = transCoordFixture();
        given(tripFindService.findOwnedTrip(10L, 1L)).willReturn(trip);
        given(transcodeClient.requestGeocode(coord)).willReturn(transCoordResponse("부산"));
        given(regionFindService.findRegion("부산")).willReturn(busan);

        tripService.certifyTrip(10L, coord, 1L);

        assertThat(trip.getCertificationAt()).isNotNull();
        then(tripCommandService).should().save(trip);
    }

    @Test
    @DisplayName("진행 중인 여행이 아니면 인증할 수 없다")
    void certifyTrip_NotInProgress() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "부산 여행");
        TransCoordDTO coord = transCoordFixture();
        given(tripFindService.findOwnedTrip(10L, 1L)).willReturn(trip);

        assertThatThrownBy(() -> tripService.certifyTrip(10L, coord, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_NOT_IN_PROGRESS);

        then(transcodeClient).should(never()).requestGeocode(any());
        then(tripCommandService).should(never()).save(any());
    }

    @Test
    @DisplayName("현재 위치의 지역을 알 수 없으면 인증할 수 없다")
    void certifyTrip_LocationUnavailable() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "부산 여행");
        trip.editStatus(TripStatus.IN_PROGRESS);
        TransCoordDTO coord = transCoordFixture();
        TransCoordResponse emptyResponse = new TransCoordResponse();
        emptyResponse.setDocuments(List.of());

        given(tripFindService.findOwnedTrip(10L, 1L)).willReturn(trip);
        given(transcodeClient.requestGeocode(coord)).willReturn(emptyResponse);

        assertThatThrownBy(() -> tripService.certifyTrip(10L, coord, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_LOCATION_UNAVAILABLE);

        then(tripCommandService).should(never()).save(any());
    }

    @Test
    @DisplayName("현재 위치가 도착 지역과 다르면 인증할 수 없다")
    void certifyTrip_LocationMismatch() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "부산 여행");
        trip.editStatus(TripStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(trip, "arrival", regionFixture(20L, "부산"));

        TransCoordDTO coord = transCoordFixture();
        given(tripFindService.findOwnedTrip(10L, 1L)).willReturn(trip);
        given(transcodeClient.requestGeocode(coord)).willReturn(transCoordResponse("서울"));
        given(regionFindService.findRegion("서울")).willReturn(regionFixture(30L, "서울"));

        assertThatThrownBy(() -> tripService.certifyTrip(10L, coord, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_LOCATION_MISMATCH);

        then(tripCommandService).should(never()).save(any());
    }

    @Test
    @DisplayName("스케줄러용 여행 상태를 변경한다")
    void changeTripStatusForScheduler() {
        TripEntity trip = tripFixture(10L, userFixture(1L), "부산 여행");
        given(tripFindService.findTrip(10L)).willReturn(trip);

        tripService.changeTripStatusForScheduler(10L, TripStatus.IN_PROGRESS);

        assertThat(trip.getStatus()).isEqualTo(TripStatus.IN_PROGRESS);
    }

    private TripEntity tripFixture(Long tripId, UserEntity user, String title) {
        TripEntity trip = TripEntity.builder()
                .user(user)
                .title(title)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 3))
                .departure(regionFixture(10L, "서울"))
                .arrival(regionFixture(20L, "부산"))
                .build();
        ReflectionTestUtils.setField(trip, "id", tripId);
        return trip;
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
        RegionEntity region = new RegionEntity(name, null, RegionLevel.SIDO);
        ReflectionTestUtils.setField(region, "id", regionId);
        return region;
    }

    private TransCoordDTO transCoordFixture() {
        TransCoordDTO coord = new TransCoordDTO();
        coord.setX("129.0756");
        coord.setY("35.1796");
        return coord;
    }

    private TransCoordResponse transCoordResponse(String regionName) {
        Address address = new Address();
        address.setRegion_1depth_name(regionName);

        TransDocument document = new TransDocument();
        document.setAddress(address);

        TransCoordResponse response = new TransCoordResponse();
        response.setDocuments(List.of(document));
        return response;
    }
}
