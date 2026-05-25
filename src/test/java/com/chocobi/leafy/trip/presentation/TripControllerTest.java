package com.chocobi.leafy.trip.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chocobi.leafy.auth.filter.JwtAuthenticationFilter;
import com.chocobi.leafy.trip.dto.request.CreateTripRequest;
import com.chocobi.leafy.trip.dto.request.TripUpdateRequest;
import com.chocobi.leafy.trip.dto.response.TripDetailResponse;
import com.chocobi.leafy.trip.dto.response.TripListResponse;
import com.chocobi.leafy.trip.dto.response.TripSaveResponse;
import com.chocobi.leafy.trip.application.TripService;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("여행을 생성한다")
    void createTrip() throws Exception {
        CreateTripRequest request = new CreateTripRequest(
                "부산 여행",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                "서울",
                "부산"
        );
        given(tripService.createTrip(any(CreateTripRequest.class), eq(1L)))
                .willReturn(TripSaveResponse.builder()
                        .tripId(10L)
                        .status(TripStatus.CREATING)
                        .build());

        mockMvc.perform(post("/api/trip")
                        .with(userAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.tripId").value(10))
                .andExpect(jsonPath("$.data.status").value("CREATING"));

        then(tripService).should().createTrip(any(CreateTripRequest.class), eq(1L));
    }

    @Test
    @DisplayName("여행 생성 요청의 제목이 비어 있으면 400을 반환한다")
    void createTrip_InvalidTitle() throws Exception {
        CreateTripRequest request = new CreateTripRequest(
                "",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                "서울",
                "부산"
        );

        mockMvc.perform(post("/api/trip")
                        .with(userAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여행 생성 요청의 종료일이 시작일보다 빠르면 400을 반환한다")
    void createTrip_InvalidDateRange() throws Exception {
        CreateTripRequest request = new CreateTripRequest(
                "부산 여행",
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 6, 1),
                "서울",
                "부산"
        );

        mockMvc.perform(post("/api/trip")
                        .with(userAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여행 목록을 조회한다")
    void getTrips() throws Exception {
        given(tripService.getTrips(1L))
                .willReturn(List.of(TripListResponse.builder()
                        .tripId(10L)
                        .title("부산 여행")
                        .startDate(LocalDate.of(2026, 6, 1))
                        .endDate(LocalDate.of(2026, 6, 3))
                        .status(TripStatus.CREATING)
                        .build()));

        mockMvc.perform(get("/api/trip")
                        .with(userAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tripId").value(10))
                .andExpect(jsonPath("$.data[0].title").value("부산 여행"));

        then(tripService).should().getTrips(1L);
    }

    @Test
    @DisplayName("여행 상세를 조회한다")
    void getTripDetails() throws Exception {
        given(tripService.getTripDetails(10L, 1L))
                .willReturn(TripDetailResponse.builder()
                        .tripId(10L)
                        .title("부산 여행")
                        .status(TripStatus.CREATING)
                        .build());

        mockMvc.perform(get("/api/trip/{tripId}", 10L)
                        .with(userAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tripId").value(10))
                .andExpect(jsonPath("$.data.title").value("부산 여행"));

        then(tripService).should().getTripDetails(10L, 1L);
    }

    @Test
    @DisplayName("여행 ID가 양수가 아니면 400을 반환한다")
    void getTripDetails_InvalidTripId() throws Exception {
        mockMvc.perform(get("/api/trip/{tripId}", 0L)
                        .with(userAuthentication()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여행을 삭제한다")
    void deleteTrip() throws Exception {
        mockMvc.perform(delete("/api/trip/{tripId}", 10L)
                        .with(userAuthentication()))
                .andExpect(status().isNoContent());

        then(tripService).should().deleteTrip(10L, 1L);
    }

    @Test
    @DisplayName("여행 정보를 수정한다")
    void updateTrip() throws Exception {
        TripUpdateRequest request = new TripUpdateRequest(
                "수정된 여행",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2)
        );
        given(tripService.updateTripInfo(eq(10L), any(TripUpdateRequest.class), eq(1L)))
                .willReturn(TripDetailResponse.builder()
                        .tripId(10L)
                        .title("수정된 여행")
                        .status(TripStatus.CREATING)
                        .build());

        mockMvc.perform(patch("/api/trip/{tripId}", 10L)
                        .with(userAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 여행"));

        then(tripService).should().updateTripInfo(eq(10L), any(TripUpdateRequest.class), eq(1L));
    }

    private RequestPostProcessor userAuthentication() {
        return request -> {
            Authentication authentication = new UsernamePasswordAuthenticationToken(1L, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setUserPrincipal(authentication);
            return request;
        };
    }
}
