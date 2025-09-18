package com.chocobi.leafy.trip.controller;

import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.TransDistanceBatchRequest;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.trip.dto.TripPlacesListRequest;
import com.chocobi.leafy.trip.dto.TripRequest;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.service.TripPlaceService;
import com.chocobi.leafy.trip.service.TripSegmentService;
import com.chocobi.leafy.trip.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;

    @PostMapping("/api/trip")
    public Long saveTrip(@RequestBody TripRequest tripRequest, Authentication authentication) {
        Long kakaoId = (Long) authentication.getPrincipal(); // 사용자 ID 가져옴
        return tripService.createTrip(tripRequest, kakaoId);
    }

    @PostMapping("/api/trip/places")
    public ResponseEntity<Map<String, String>> saveTripPlaces(@RequestBody TripPlacesListRequest tripPlaceListRequest) {
        tripPlaceService.saveTripPlaces(tripPlaceListRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "여행지가 성공적으로 저장되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/trip/places")
    public ResponseEntity<Map<String, String>> updateTripPlaceDetails(@RequestBody TripPlacesListRequest tripPlaceListRequest) {
        tripPlaceService.updateTripPlaceDetails(tripPlaceListRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "여행지 정보가 성공적으로 업데이트되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/trip/{tripId}/complete")
    public ResponseEntity<Map<String, Object>> completeTrip(@PathVariable Long tripId, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            String transport = request.get("transport");
            tripSegmentService.completeTripSegments(tripId, transport);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "여행 계획이 성공적으로 완료되었습니다.");
            response.put("tripId", tripId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "여행 계획 완료 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/api/trip/{tripId}/summary")
    public ResponseEntity<Map<String, Object>> getTripSummary(@PathVariable Long tripId, @RequestParam String transport, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            Map<String, Object> summary = tripSegmentService.getTotalTimeAndCarbon(tripId, transport);
            
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "여행 요약 정보를 가져오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/api/trip/{tripId}/routes/car")
    public ResponseEntity<DistanceResponse> calculateCarRoute(@PathVariable Long tripId, @RequestBody CarDistanceRequest request, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            DistanceResponse response = tripSegmentService.calculateAndSaveCarRoute(request, tripId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("자동차 경로 계산 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/api/trip/routes/public")
    public ResponseEntity<List<RouteCalculationResult>> calculatePublicRoute(@RequestBody TransDistanceBatchRequest request, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            List<RouteCalculationResult> results = tripSegmentService.calculateAndSavePublicRoute(request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("대중교통 경로 계산 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
