package com.chocobi.leafy.trip.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionLevel;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.vo.PlaceError;
import com.chocobi.leafy.trip.dto.request.CreateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.request.UpdateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceLocationResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.response.TripPlacesResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripPlaceCommandService;
import com.chocobi.leafy.trip.infra.TripPlaceFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.vo.TripPlaceError;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.entity.enums.Provider;
import java.time.LocalDate;
import java.util.ArrayList;
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
class TripPlaceServiceTest {

    @InjectMocks
    private TripPlaceService tripPlaceService;

    @Mock
    private TripPlaceFindService tripPlaceFindService;

    @Mock
    private TripPlaceCommandService tripPlaceCommandService;

    @Mock
    private PlaceService placeService;

    @Mock
    private TripFindService tripFindService;

    @Test
    @DisplayName("외부 장소를 여행 장소 위치 응답으로 변환한다")
    void tripPlaceLocationResponseFromExternalPlace() {
        ExternalPlaceEntity place = externalPlaceFixture(10L, "해운대");

        TripPlaceLocationResponse result = TripPlaceLocationResponse.from(place);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("해운대");
        assertThat(result.getAddress()).isEqualTo("해운대 주소");
        assertThat(result.getLatitude()).isEqualTo(37.5);
        assertThat(result.getLongitude()).isEqualTo(127.1);
    }

    @Test
    @DisplayName("커스텀 장소를 여행 장소 위치 응답으로 변환한다")
    void tripPlaceLocationResponseFromCustomPlace() {
        CustomPlaceEntity place = customPlaceFixture(20L, "나만의 장소");

        TripPlaceLocationResponse result = TripPlaceLocationResponse.from(place);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getTitle()).isEqualTo("나만의 장소");
        assertThat(result.getAddress()).isEqualTo("나만의 장소 주소");
        assertThat(result.getLatitude()).isEqualTo(35.1);
        assertThat(result.getLongitude()).isEqualTo(129.1);
    }

    @Test
    @DisplayName("여행 장소를 최초 생성한다")
    void createTripPlaces() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity firstPlace = externalPlaceFixture(10L, "해운대");
        ExternalPlaceEntity secondPlace = externalPlaceFixture(20L, "광안리");
        List<CreateTripPlaceRequest> request = List.of(
                new CreateTripPlaceRequest(10L, 0, 0, "첫 번째"),
                new CreateTripPlaceRequest(20L, 1, 0, "두 번째")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.hasTripPlaces(1L)).willReturn(false);
        given(placeService.getPlaces(List.of(10L, 20L))).willReturn(List.of(firstPlace, secondPlace));
        given(tripPlaceCommandService.saveAll(anyList())).willAnswer(invocation -> {
            List<TripPlaceEntity> tripPlaces = invocation.getArgument(0);
            for (int i = 0; i < tripPlaces.size(); i++) {
                ReflectionTestUtils.setField(tripPlaces.get(i), "id", (long) i + 1);
            }
            return tripPlaces;
        });

        TripPlacesResponse result = tripPlaceService.createTripPlaces(1L, request, 100L);

        assertThat(result.getTripId()).isEqualTo(1L);
        assertThat(result.isRouteStale()).isTrue();
        assertThat(result.getTripPlaces()).hasSize(2);
        assertThat(result.getTripPlaces().getFirst().getPlace().getTitle()).isEqualTo("해운대");

        ArgumentCaptor<List<TripPlaceEntity>> tripPlacesCaptor = ArgumentCaptor.forClass(List.class);
        then(tripPlaceCommandService).should().saveAll(tripPlacesCaptor.capture());
        List<TripPlaceEntity> savedTripPlaces = tripPlacesCaptor.getValue();
        assertThat(savedTripPlaces).hasSize(2);
        assertThat(savedTripPlaces.getFirst().getTrip()).isEqualTo(trip);
        assertThat(savedTripPlaces.getFirst().getPlace()).isEqualTo(firstPlace);
        assertThat(savedTripPlaces.getFirst().getMemo()).isEqualTo("첫 번째");
        assertThat(savedTripPlaces.getFirst().getDayIndex()).isZero();
        assertThat(savedTripPlaces.getFirst().getVisitOrder()).isZero();
    }

    @Test
    @DisplayName("커스텀 장소로 여행 장소를 생성한다")
    void createTripPlacesWithCustomPlace() {
        TripEntity trip = tripFixture(1L);
        CustomPlaceEntity place = customPlaceFixture(10L, "나만의 장소");
        List<CreateTripPlaceRequest> request = List.of(new CreateTripPlaceRequest(10L, 0, 0, "메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.hasTripPlaces(1L)).willReturn(false);
        given(placeService.getPlaces(List.of(10L))).willReturn(List.of(place));
        given(tripPlaceCommandService.saveAll(anyList())).willAnswer(invocation -> {
            List<TripPlaceEntity> tripPlaces = invocation.getArgument(0);
            ReflectionTestUtils.setField(tripPlaces.getFirst(), "id", 1L);
            return tripPlaces;
        });

        TripPlacesResponse result = tripPlaceService.createTripPlaces(1L, request, 100L);

        assertThat(result.getTripPlaces()).hasSize(1);
        assertThat(result.getTripPlaces().getFirst().getPlace().getTitle()).isEqualTo("나만의 장소");
    }

    @Test
    @DisplayName("생성 요청에 중복 방문 순서가 있으면 예외가 발생한다")
    void createTripPlacesDuplicateVisitOrder() {
        TripEntity trip = tripFixture(1L);
        List<CreateTripPlaceRequest> request = List.of(
                new CreateTripPlaceRequest(10L, 0, 0, "첫 번째"),
                new CreateTripPlaceRequest(20L, 0, 0, "두 번째")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);

        assertThatThrownBy(() -> tripPlaceService.createTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.DUPLICATE_TRIP_PLACE_REQUEST);

        then(tripPlaceCommandService).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("이미 여행 장소가 있으면 최초 생성할 수 없다")
    void createTripPlacesAlreadyExist() {
        TripEntity trip = tripFixture(1L);
        List<CreateTripPlaceRequest> request = List.of(new CreateTripPlaceRequest(10L, 0, 0, "메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.hasTripPlaces(1L)).willReturn(true);

        assertThatThrownBy(() -> tripPlaceService.createTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.TRIP_PLACES_ALREADY_EXIST);

        then(placeService).should(never()).getPlaces(anyList());
        then(tripPlaceCommandService).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("생성 요청의 장소 일부가 없으면 예외가 발생한다")
    void createTripPlacesPlaceNotFound() {
        TripEntity trip = tripFixture(1L);
        List<CreateTripPlaceRequest> request = List.of(
                new CreateTripPlaceRequest(10L, 0, 0, "첫 번째"),
                new CreateTripPlaceRequest(20L, 1, 0, "두 번째")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.hasTripPlaces(1L)).willReturn(false);
        given(placeService.getPlaces(List.of(10L, 20L))).willReturn(List.of(externalPlaceFixture(10L, "해운대")));

        assertThatThrownBy(() -> tripPlaceService.createTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceError.PLACE_NOT_FOUND);

        then(tripPlaceCommandService).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("생성 요청의 중복 장소 ID는 한 번만 조회한다")
    void createTripPlacesUsesDistinctPlaceIds() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity place = externalPlaceFixture(10L, "해운대");
        List<CreateTripPlaceRequest> request = List.of(
                new CreateTripPlaceRequest(10L, 0, 0, "첫 번째"),
                new CreateTripPlaceRequest(10L, 1, 0, "두 번째")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.hasTripPlaces(1L)).willReturn(false);
        given(placeService.getPlaces(List.of(10L))).willReturn(List.of(place));
        given(tripPlaceCommandService.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        tripPlaceService.createTripPlaces(1L, request, 100L);

        then(placeService).should().getPlaces(List.of(10L));
    }

    @Test
    @DisplayName("메모만 수정하면 경로 재계산이 필요하지 않다")
    void updateTripPlacesMemoOnly() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity place = externalPlaceFixture(10L, "해운대");
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, place, 0, 0, "기존 메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 10L, 0, 0, "수정 메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(existing.getMemo()).isEqualTo("수정 메모");
        assertThat(result.isRouteStale()).isFalse();
        then(placeService).should(never()).getPlaces(anyList());
        then(tripPlaceCommandService).should(never()).saveAll(anyList());
        then(tripPlaceCommandService).should(never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("방문 일정이 바뀌면 경로 재계산이 필요하다")
    void updateTripPlacesScheduleChanged() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity place = externalPlaceFixture(10L, "해운대");
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, place, 0, 0, "기존 메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 10L, 1, 1, "수정 메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(existing.getDayIndex()).isEqualTo(1);
        assertThat(existing.getVisitOrder()).isEqualTo(1);
        assertThat(existing.getMemo()).isEqualTo("수정 메모");
        assertThat(result.isRouteStale()).isTrue();
        then(placeService).should(never()).getPlaces(anyList());
    }

    @Test
    @DisplayName("장소를 교체하면 새 장소로 수정하고 경로 재계산이 필요하다")
    void updateTripPlacesPlaceChanged() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity oldPlace = externalPlaceFixture(10L, "해운대");
        ExternalPlaceEntity newPlace = externalPlaceFixture(20L, "광안리");
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, oldPlace, 0, 0, "기존 메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 20L, 1, 1, "수정 메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));
        given(placeService.getPlaces(List.of(20L))).willReturn(List.of(newPlace));

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(existing.getPlace()).isEqualTo(newPlace);
        assertThat(existing.getDayIndex()).isEqualTo(1);
        assertThat(existing.getVisitOrder()).isEqualTo(1);
        assertThat(existing.getMemo()).isEqualTo("수정 메모");
        assertThat(result.isRouteStale()).isTrue();
    }

    @Test
    @DisplayName("외부 장소를 커스텀 장소로 교체한다")
    void updateTripPlacesExternalPlaceToCustomPlace() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity oldPlace = externalPlaceFixture(10L, "해운대");
        CustomPlaceEntity newPlace = customPlaceFixture(20L, "나만의 장소");
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, oldPlace, 0, 0, "기존 메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 20L, 0, 0, "수정 메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));
        given(placeService.getPlaces(List.of(20L))).willReturn(List.of(newPlace));

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(result.getTripPlaces()).hasSize(1);
        assertThat(result.getTripPlaces().getFirst().getPlace().getTitle()).isEqualTo("나만의 장소");
        assertThat(result.isRouteStale()).isTrue();
    }

    @Test
    @DisplayName("신규 여행 장소를 추가한다")
    void updateTripPlacesAddNewPlace() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity existingPlace = externalPlaceFixture(10L, "해운대");
        ExternalPlaceEntity newPlace = externalPlaceFixture(20L, "광안리");
        List<TripPlaceEntity> existingTripPlaces = new ArrayList<>();
        existingTripPlaces.add(tripPlaceFixture(100L, trip, existingPlace, 0, 0, "기존 메모"));
        List<UpdateTripPlaceRequest> request = List.of(
                new UpdateTripPlaceRequest(100L, 10L, 0, 0, "기존 메모"),
                new UpdateTripPlaceRequest(null, 20L, 1, 0, "신규 메모")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(existingTripPlaces);
        given(placeService.getPlaces(List.of(10L, 20L))).willReturn(List.of(existingPlace, newPlace));
        given(tripPlaceCommandService.saveAll(anyList())).willAnswer(invocation -> {
            List<TripPlaceEntity> newTripPlaces = invocation.getArgument(0);
            ReflectionTestUtils.setField(newTripPlaces.getFirst(), "id", 200L);
            existingTripPlaces.addAll(newTripPlaces);
            return newTripPlaces;
        });

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(result.isRouteStale()).isTrue();
        assertThat(result.getTripPlaces()).hasSize(2);
        assertThat(result.getTripPlaces().get(1).getPlace().getTitle()).isEqualTo("광안리");
        then(tripPlaceCommandService).should().saveAll(anyList());
    }

    @Test
    @DisplayName("기존 여행 장소를 삭제한다")
    void updateTripPlacesDeletePlace() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity firstPlace = externalPlaceFixture(10L, "해운대");
        ExternalPlaceEntity secondPlace = externalPlaceFixture(20L, "광안리");
        TripPlaceEntity first = tripPlaceFixture(100L, trip, firstPlace, 0, 0, "첫 번째");
        TripPlaceEntity second = tripPlaceFixture(200L, trip, secondPlace, 0, 1, "두 번째");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 10L, 0, 0, "첫 번째"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(first, second), List.of(first));

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(result.isRouteStale()).isTrue();
        assertThat(result.getTripPlaces()).hasSize(1);
        then(tripPlaceCommandService).should().deleteAll(List.of(second));
    }

    @Test
    @DisplayName("수정, 삭제, 신규 추가를 함께 처리한다")
    void updateTripPlacesMixedChanges() {
        TripEntity trip = tripFixture(1L);
        ExternalPlaceEntity firstPlace = externalPlaceFixture(10L, "해운대");
        ExternalPlaceEntity secondPlace = externalPlaceFixture(20L, "광안리");
        ExternalPlaceEntity newPlace = externalPlaceFixture(30L, "송정");
        TripPlaceEntity first = tripPlaceFixture(100L, trip, firstPlace, 0, 0, "첫 번째");
        TripPlaceEntity second = tripPlaceFixture(200L, trip, secondPlace, 0, 1, "두 번째");
        List<TripPlaceEntity> latestTripPlaces = new ArrayList<>(List.of(first));
        List<UpdateTripPlaceRequest> request = List.of(
                new UpdateTripPlaceRequest(100L, 10L, 0, 1, "수정"),
                new UpdateTripPlaceRequest(null, 30L, 1, 1, "신규")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(first, second), latestTripPlaces);
        given(placeService.getPlaces(List.of(10L, 30L))).willReturn(List.of(firstPlace, newPlace));
        given(tripPlaceCommandService.saveAll(anyList())).willAnswer(invocation -> {
            List<TripPlaceEntity> newTripPlaces = invocation.getArgument(0);
            ReflectionTestUtils.setField(newTripPlaces.getFirst(), "id", 300L);
            latestTripPlaces.addAll(newTripPlaces);
            return newTripPlaces;
        });

        TripPlacesResponse result = tripPlaceService.updateTripPlaces(1L, request, 100L);

        assertThat(first.getDayIndex()).isEqualTo(1);
        assertThat(first.getMemo()).isEqualTo("수정");
        assertThat(result.isRouteStale()).isTrue();
        assertThat(result.getTripPlaces()).hasSize(2);
        then(tripPlaceCommandService).should().deleteAll(List.of(second));
        then(tripPlaceCommandService).should().saveAll(anyList());
    }

    @Test
    @DisplayName("수정 요청에 중복 여행 장소 ID가 있으면 예외가 발생한다")
    void updateTripPlacesDuplicateTripPlaceId() {
        TripEntity trip = tripFixture(1L);
        List<UpdateTripPlaceRequest> request = List.of(
                new UpdateTripPlaceRequest(100L, 10L, 0, 0, "첫 번째"),
                new UpdateTripPlaceRequest(100L, 20L, 1, 0, "두 번째")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);

        assertThatThrownBy(() -> tripPlaceService.updateTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.DUPLICATE_TRIP_PLACE_REQUEST);
    }

    @Test
    @DisplayName("수정 요청에 중복 방문 순서가 있으면 예외가 발생한다")
    void updateTripPlacesDuplicateVisitOrder() {
        TripEntity trip = tripFixture(1L);
        List<UpdateTripPlaceRequest> request = List.of(
                new UpdateTripPlaceRequest(100L, 10L, 0, 0, "첫 번째"),
                new UpdateTripPlaceRequest(200L, 20L, 0, 0, "두 번째")
        );

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);

        assertThatThrownBy(() -> tripPlaceService.updateTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.DUPLICATE_TRIP_PLACE_REQUEST);
    }

    @Test
    @DisplayName("기존 여행 장소가 없으면 수정할 수 없다")
    void updateTripPlacesNotCreated() {
        TripEntity trip = tripFixture(1L);
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 10L, 0, 0, "메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of());

        assertThatThrownBy(() -> tripPlaceService.updateTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.TRIP_PLACES_NOT_CREATED);
    }

    @Test
    @DisplayName("요청한 여행 장소 ID가 기존 목록에 없으면 예외가 발생한다")
    void updateTripPlacesTripPlaceNotFound() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, externalPlaceFixture(10L, "해운대"), 0, 0, "메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(999L, 10L, 0, 0, "메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));

        assertThatThrownBy(() -> tripPlaceService.updateTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.TRIP_PLACE_NOT_FOUND);
    }

    @Test
    @DisplayName("수정에 필요한 장소가 없으면 예외가 발생한다")
    void updateTripPlacesPlaceNotFound() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, externalPlaceFixture(10L, "해운대"), 0, 0, "메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 20L, 0, 0, "메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));
        given(placeService.getPlaces(List.of(20L))).willReturn(List.of());

        assertThatThrownBy(() -> tripPlaceService.updateTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceError.PLACE_NOT_FOUND);

        then(tripPlaceCommandService).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("기존 여행 장소의 장소가 없으면 예외가 발생한다")
    void updateTripPlacesExistingPlaceIsNull() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity existing = tripPlaceFixture(100L, trip, null, 0, 0, "메모");
        List<UpdateTripPlaceRequest> request = List.of(new UpdateTripPlaceRequest(100L, 10L, 0, 0, "메모"));

        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(existing));

        assertThatThrownBy(() -> tripPlaceService.updateTripPlaces(1L, request, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.TRIP_PLACE_NOT_FOUND);
    }

    @Test
    @DisplayName("여행 장소 목록을 응답 DTO로 변환한다")
    void getTripPlaces() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity first = tripPlaceFixture(100L, trip, externalPlaceFixture(10L, "해운대"), 0, 0, "첫 번째");
        TripPlaceEntity second = tripPlaceFixture(200L, trip, customPlaceFixture(20L, "나만의 장소"), 0, 1, "두 번째");

        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(first, second));

        List<TripPlaceResponse> result = tripPlaceService.getTripPlaces(1L);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getPlace().getTitle()).isEqualTo("해운대");
        assertThat(result.get(1).getPlace().getTitle()).isEqualTo("나만의 장소");
    }

    @Test
    @DisplayName("여행 장소 목록 조회 중 장소가 없으면 예외가 발생한다")
    void getTripPlacesPlaceIsNull() {
        TripEntity trip = tripFixture(1L);
        TripPlaceEntity tripPlace = tripPlaceFixture(100L, trip, null, 0, 0, "메모");

        given(tripPlaceFindService.findOrderedTripPlaces(1L)).willReturn(List.of(tripPlace));

        assertThatThrownBy(() -> tripPlaceService.getTripPlaces(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripPlaceError.TRIP_PLACE_NOT_FOUND);
    }

    @Test
    @DisplayName("여행 장소 단건 조회 결과를 반환한다")
    void getTripPlaceById() {
        TripPlaceEntity tripPlace = tripPlaceFixture(
                100L,
                tripFixture(1L),
                externalPlaceFixture(10L, "해운대"),
                0,
                0,
                "메모"
        );

        given(tripPlaceFindService.findTripPlace(100L)).willReturn(tripPlace);

        TripPlaceEntity result = tripPlaceService.getTripPlaceById(100L);

        assertThat(result).isEqualTo(tripPlace);
    }

    private TripEntity tripFixture(Long tripId) {
        TripEntity trip = TripEntity.builder()
                .user(userFixture(100L))
                .title("부산 여행")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 3))
                .departure(regionFixture(10L, "서울"))
                .arrival(regionFixture(20L, "부산"))
                .build();
        ReflectionTestUtils.setField(trip, "id", tripId);
        return trip;
    }

    private TripPlaceEntity tripPlaceFixture(
            Long tripPlaceId,
            TripEntity trip,
            PlaceEntity place,
            int dayIndex,
            int visitOrder,
            String memo
    ) {
        TripPlaceEntity tripPlace = TripPlaceEntity.builder()
                .trip(trip)
                .place(place)
                .dayIndex(dayIndex)
                .visitOrder(visitOrder)
                .memo(memo)
                .build();
        ReflectionTestUtils.setField(tripPlace, "id", tripPlaceId);
        return tripPlace;
    }

    private ExternalPlaceEntity externalPlaceFixture(Long placeId, String title) {
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

    private CustomPlaceEntity customPlaceFixture(Long placeId, String title) {
        CustomPlaceEntity place = CustomPlaceEntity.builder()
                .title(title)
                .address(title + " 주소")
                .latitude(35.1)
                .longitude(129.1)
                .copyright("테스트")
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
        RegionEntity region = new RegionEntity(name, null, RegionLevel.SIDO);
        ReflectionTestUtils.setField(region, "id", regionId);
        return region;
    }
}
