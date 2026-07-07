package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.TransDistanceBatchRequest;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.application.TripMessageService;
import com.chocobi.leafy.trip.application.TripRouteCandidateService;
import com.chocobi.leafy.trip.application.TripSegmentService;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripRouteController {

    private final TripRouteCandidateService tripRouteCandidateService;
    private final TripSegmentService tripSegmentService;
    private final TripMessageService tripMessageService;

    @PostMapping("/{tripId}/complete")
    public ResponseEntity<Map<String, Object>> completeTrip(@PathVariable @Positive Long tripId,
                                                            @RequestBody Map<String, String> request,
                                                            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            tripRouteCandidateService.completeOwnedRouteCandidate(tripId, request.get("transport"), userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "여행 계획이 성공적으로 완료되었습니다.");
            response.put("tripId", tripId);

            tripMessageService.notifyTripCreated(userId, tripId);

            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "여행 계획 완료 중 오류가 발생했습니다."
            ));
        }
    }

    @GetMapping("/{tripId}/summary")
    public ResponseEntity<TripRouteCandidateSummaryResponse> getTripSummary(@PathVariable @Positive Long tripId,
                                                                   @RequestParam String transport,
                                                                   Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            return ResponseEntity.ok(tripRouteCandidateService.getOwnedRouteSummary(tripId, transport, userId));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{tripId}/routes/car")
    public ResponseEntity<DistanceResponse> calculateCarRoute(@PathVariable @Positive Long tripId,
                                                              Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            return ResponseEntity.ok(tripSegmentService.calculateAndSaveOwnedCarRoute(tripId, userId));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("자동차 경로 계산 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/routes/public")
    public ResponseEntity<List<RouteCalculationResult>> calculatePublicRoute(@RequestBody TransDistanceBatchRequest request,
                                                                            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            return ResponseEntity.ok(tripSegmentService.calculateAndSaveOwnedPublicRoute(request, userId));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("대중교통 경로 계산 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
