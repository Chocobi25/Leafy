package com.chocobi.leafy.trip.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chocobi.leafy.auth.filter.JwtAuthenticationFilter;
import com.chocobi.leafy.trip.application.TripRouteService;
import com.chocobi.leafy.trip.dto.request.CompleteTripRequest;
import com.chocobi.leafy.trip.dto.request.TripRouteSummaryRequest;
import com.chocobi.leafy.trip.dto.response.CompleteTripResponse;
import com.chocobi.leafy.trip.dto.response.TripCarRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripPublicRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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

@WebMvcTest(TripRouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripRouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripRouteService tripRouteService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("여행 경로 후보를 확정하고 완료한다")
    void completeTrip() throws Exception {
        given(tripRouteService.completeTrip(eq(10L), any(CompleteTripRequest.class), eq(1L)))
                .willReturn(CompleteTripResponse.from(10L));

        mockMvc.perform(post("/api/trip/{tripId}/complete", 10L)
                        .with(userAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "transport": "car"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.tripId").value(10));

        then(tripRouteService).should().completeTrip(eq(10L), any(CompleteTripRequest.class), eq(1L));
    }

    @Test
    @DisplayName("여행 경로 후보 요약을 조회한다")
    void getRouteSummary() throws Exception {
        given(tripRouteService.getRouteSummary(eq(10L), any(TripRouteSummaryRequest.class), eq(1L)))
                .willReturn(TripRouteCandidateSummaryResponse.builder()
                        .totalDuration(30)
                        .totalCarbonEmission(2.5)
                        .build());

        mockMvc.perform(get("/api/trip/{tripId}/summary", 10L)
                        .param("transport", "PUBLIC")
                        .with(userAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDuration").value(30))
                .andExpect(jsonPath("$.data.totalCarbonEmission").value(2.5));

        then(tripRouteService).should().getRouteSummary(eq(10L), any(TripRouteSummaryRequest.class), eq(1L));
    }

    @Test
    @DisplayName("자동차 경로를 계산하고 후보로 저장한다")
    void calculateCarRoute() throws Exception {
        given(tripRouteService.calculateCarRoute(10L, 1L))
                .willReturn(TripCarRouteResponse.builder()
                        .distance(12000.0)
                        .duration(2400)
                        .carbonEmission(2.3)
                        .build());

        mockMvc.perform(post("/api/trip/{tripId}/routes/car", 10L)
                        .with(userAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.distance").value(12000.0))
                .andExpect(jsonPath("$.data.duration").value(2400))
                .andExpect(jsonPath("$.data.carbonEmission").value(2.3));

        then(tripRouteService).should().calculateCarRoute(10L, 1L);
    }

    @Test
    @DisplayName("대중교통 경로를 계산하고 후보로 저장한다")
    void calculatePublicRoute() throws Exception {
        given(tripRouteService.calculatePublicRoute(10L, 1L))
                .willReturn(List.of(TripPublicRouteResponse.builder()
                        .pathType(1)
                        .totalTime(3600)
                        .totalDistance(15000.0)
                        .carbonEmission(1.2)
                        .maxCarbonEmission(4.8)
                        .busDistance(5000)
                        .subwayDistance(8000)
                        .trainDistance(2000)
                        .airplaneDistance(0)
                        .build()));

        mockMvc.perform(post("/api/trip/{tripId}/routes/public", 10L)
                        .with(userAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].pathType").value(1))
                .andExpect(jsonPath("$.data[0].totalTime").value(3600))
                .andExpect(jsonPath("$.data[0].totalDistance").value(15000.0));

        then(tripRouteService).should().calculatePublicRoute(10L, 1L);
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
