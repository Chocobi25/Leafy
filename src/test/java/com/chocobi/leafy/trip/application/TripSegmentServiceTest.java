package com.chocobi.leafy.trip.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.service.TransDistanceService;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripRouteOptionCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentCommandService;
import com.chocobi.leafy.trip.infra.TripSegmentFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.vo.TripError;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripSegmentServiceTest {

    @InjectMocks
    private TripSegmentService tripSegmentService;

    @Mock
    private TripSegmentFindService tripSegmentFindService;

    @Mock
    private TripSegmentCommandService tripSegmentCommandService;

    @Mock
    private TripRouteOptionCommandService tripRouteOptionCommandService;

    @Mock
    private TripRouteCandidateService tripRouteCandidateService;

    @Mock
    private TripFindService tripFindService;

    @Mock
    private CarDistanceService carDistanceService;

    @Mock
    private TransDistanceService transDistanceService;

    @Mock
    private TripPlaceService tripPlaceService;

    @Test
    @DisplayName("여행 장소가 2개 미만이면 자동차 외부 경로 API를 호출하지 않는다")
    void calculateOwnedCarRouteWithInsufficientTripPlaces() {
        TripEntity trip = mock(TripEntity.class);
        given(trip.isEditable()).willReturn(true);
        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceService.getTripPlaces(1L)).willReturn(List.of());

        assertThatThrownBy(() -> tripSegmentService.calculateAndSaveOwnedCarRoute(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.INVALID_TRIP_ROUTE_CANDIDATE);

        then(carDistanceService).should(never()).calculateTripDistance(1L, List.of());
    }

    @Test
    @DisplayName("진행 중인 여행은 자동차 경로를 다시 계산할 수 없다")
    void calculateOwnedCarRouteInProgress() {
        TripEntity trip = mock(TripEntity.class);
        given(trip.isEditable()).willReturn(false);
        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);

        assertThatThrownBy(() -> tripSegmentService.calculateAndSaveOwnedCarRoute(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.TRIP_NOT_EDITABLE);

        then(tripPlaceService).should(never()).getTripPlaces(1L);
        then(carDistanceService).should(never()).calculateTripDistance(1L, List.of());
    }

    @Test
    @DisplayName("여행 장소가 2개 미만이면 대중교통 외부 경로 API를 호출하지 않는다")
    void calculateOwnedPublicRouteWithInsufficientTripPlaces() {
        TripEntity trip = mock(TripEntity.class);
        given(trip.isEditable()).willReturn(true);
        given(tripFindService.findOwnedTrip(1L, 100L)).willReturn(trip);
        given(tripPlaceService.getTripPlaces(1L)).willReturn(List.of());

        assertThatThrownBy(() -> tripSegmentService.calculateAndSaveOwnedPublicRoute(1L, 100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TripError.INVALID_TRIP_ROUTE_CANDIDATE);

        then(transDistanceService).should(never()).calculateTripDistance(1L, List.of());
    }
}
