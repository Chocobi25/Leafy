package com.chocobi.leafy.distance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chocobi.leafy.distance.domain.TransDistanceRequest;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.trip.application.TripPlaceService;
import com.chocobi.leafy.trip.dto.response.TripPlaceLocationResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class TransDistanceServiceTest {

    @Test
    @DisplayName("대중교통 경로는 저장된 장소 순서대로 구간 요청을 만든다")
    void calculateTripDistanceKeepsOrderedSegments() {
        List<TransDistanceRequest> capturedRequests = new ArrayList<>();
        TransDistanceService service = new CapturingTransDistanceService(capturedRequests);

        service.calculateTripDistance(1L, List.of(
                tripPlace(3L, 2, 2, 33.5, 126.5),
                tripPlace(1L, 1, 1, 37.5, 127.0),
                tripPlace(2L, 2, 1, 36.0, 128.0)
        ));

        assertThat(capturedRequests).hasSize(2);
        assertThat(capturedRequests.get(0).getStartX()).isEqualTo("127.0");
        assertThat(capturedRequests.get(0).getStartY()).isEqualTo("37.5");
        assertThat(capturedRequests.get(0).getEndX()).isEqualTo("128.0");
        assertThat(capturedRequests.get(0).getEndY()).isEqualTo("36.0");
        assertThat(capturedRequests.get(1).getStartX()).isEqualTo("128.0");
        assertThat(capturedRequests.get(1).getStartY()).isEqualTo("36.0");
        assertThat(capturedRequests.get(1).getEndX()).isEqualTo("126.5");
        assertThat(capturedRequests.get(1).getEndY()).isEqualTo("33.5");
    }

    private TripPlaceResponse tripPlace(Long id, int visitOrder, int dayIndex, double latitude, double longitude) {
        return TripPlaceResponse.builder()
                .tripPlaceId(id)
                .visitOrder(visitOrder)
                .dayIndex(dayIndex)
                .place(TripPlaceLocationResponse.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .build())
                .build();
    }

    private static class CapturingTransDistanceService extends TransDistanceService {
        private final List<TransDistanceRequest> capturedRequests;

        private CapturingTransDistanceService(List<TransDistanceRequest> capturedRequests) {
            super(mock(WebClient.class), mock(TripPlaceService.class));
            this.capturedRequests = capturedRequests;
        }

        @Override
        public RouteCalculationResult getDistance(TransDistanceRequest request, boolean isJejuTrip) {
            capturedRequests.add(request);
            return new RouteCalculationResult();
        }
    }
}
