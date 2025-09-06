package com.chocobi.leafy.trip.controller;

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
    public ResponseEntity<Map<String, Object>> getTripSummary(@PathVariable Long tripId, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            Map<String, Object> summary = tripSegmentService.getTotalTimeAndCarbon(tripId);
            
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

    // Test용 메서드(수정할 예정)
    @PostMapping("/api/test/trip")
    public ResponseEntity<Trip> createTrip(@RequestBody TripRequest tripRequest, Authentication authentication) {
        // 여행 생성
        Long createdTripId = tripService.createTrip(tripRequest, (Long) authentication.getPrincipal());

        // 생성된 여행 정보 조회
        Trip createdTrip = tripService.getTripById(createdTripId);

        // HTTP 상태 코드와 함께 응답
        if (createdTrip != null) {
            return new ResponseEntity<>(createdTrip, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
