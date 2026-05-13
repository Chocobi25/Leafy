package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.domain.TransDistanceBatchRequest;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.trip.client.TransCoordDTO;
import com.chocobi.leafy.trip.dto.TripDetailsDTO;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.request.TripPlacesListRequest;
import com.chocobi.leafy.trip.dto.request.TripRequest;
import com.chocobi.leafy.trip.dto.request.RecalculateRoutesRequest;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.application.TripMessageService;
import com.chocobi.leafy.trip.application.TripPlaceService;
import com.chocobi.leafy.trip.application.TripSegmentService;
import com.chocobi.leafy.trip.application.TripService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trip")
@AllArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;
    private final TripMessageService tripMessageService;

    @PostMapping
    public Long saveTrip(@RequestBody TripRequest tripRequest, Authentication authentication) {
        Long kakaoId = (Long) authentication.getPrincipal();
        return tripService.createTrip(tripRequest, kakaoId);
    }

    @PostMapping("/places")
    public ResponseEntity<Map<String, String>> saveTripPlaces(@RequestBody TripPlacesListRequest tripPlaceListRequest) {
        TripEntity trip = tripService.getTripById(tripPlaceListRequest.getTripId());

        tripPlaceService.saveInitialTripPlaces(trip, tripPlaceListRequest);

        Map<String, String> response = new HashMap<>();
        response.put("message", "여행지가 성공적으로 저장되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/places")
    public ResponseEntity<Map<String, String>> updateTripPlaceDetails(@RequestBody TripPlacesListRequest tripPlaceListRequest) {
        TripEntity trip = tripService.getTripById(tripPlaceListRequest.getTripId());
        tripPlaceService.editTripPlaceDetails(trip, tripPlaceListRequest.getPlaces());
        Map<String, String> response = new HashMap<>();
        response.put("message", "여행지 정보가 성공적으로 업데이트되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/edit")
    public ResponseEntity<Map<String, String>> editTripPlaceDetails(@RequestBody RecalculateRoutesRequest request) {
        TripEntity trip = tripService.getTripById(request.getTripId());

        // 1. TripPlace 업데이트
        tripPlaceService.editTripPlaceDetails(trip, request.getPlaces());

        // 2. 🔥 DB에서 업데이트된 TripPlace 다시 조회
        List<TripPlaceResponse> updatedTripPlaces = tripPlaceService.getTripPlaces(trip.getId());

        // 3. TripSegment 재계산 (업데이트된 데이터 사용)
        tripSegmentService.recalculateRoutesAndSaveV2(trip, request.getTransport(), updatedTripPlaces);

        // 4. Redis에 있는 임시 세그먼트를 DB에 저장
        tripSegmentService.completeTripSegments(trip.getId(), request.getTransport());

        Map<String, String> response = new HashMap<>();
        response.put("message", "여행지 정보 및 경로가 성공적으로 업데이트되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tripId}/complete")
    public ResponseEntity<Map<String, Object>> completeTrip(@PathVariable Long tripId, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            String transport = request.get("transport");
            tripSegmentService.completeTripSegments(tripId, transport);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "여행 계획이 성공적으로 완료되었습니다.");
            response.put("tripId", tripId);

            tripService.changeTripStatus(tripId, TripStatus.READY);
            tripMessageService.notifyTripCreated(kakaoId, tripId);

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

    @GetMapping("/{tripId}/summary")
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


    @PostMapping("/{tripId}/routes/car")
    public ResponseEntity<DistanceResponse> calculateCarRoute(@PathVariable Long tripId, @RequestBody CarDistanceRequest request, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();
            System.out.println("자동차 경로 계산 요청 - tripId: " + tripId + ", request: " + request);
            DistanceResponse response = tripSegmentService.calculateAndSaveCarRoute(request, tripId);
            System.out.println("자동차 경로 계산 완료 - response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("자동차 경로 계산 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/routes/public")
    public ResponseEntity<List<RouteCalculationResult>> calculatePublicRoute(@RequestBody TransDistanceBatchRequest request, Authentication authentication) {
        try {
            Long kakaoId = (Long) authentication.getPrincipal();

            List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(request.getTripId());
            List<RouteCalculationResult> results = tripSegmentService.calculateAndSavePublicRoute(request, tripPlaces);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("대중교통 경로 계산 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/details/{tripId}")
    public ResponseEntity<TripDetailsDTO> getTripDetails(@PathVariable Long tripId) {
        TripDetailsDTO tripDetails = tripService.getTripDetails(tripId);
        return ResponseEntity.ok(tripDetails);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<String> deleteTrip(@PathVariable Long tripId) {
        try {
            tripService.deleteTrip(tripId);
            return ResponseEntity.ok("여행 기록이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("여행 기록 삭제 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/{tripId}/certify")
    public ResponseEntity<?> certifyTrip(@RequestBody TransCoordDTO transCoordDTO) {
        try {
            tripService.certifyTrip(transCoordDTO);
            return ResponseEntity.ok("여행이 성공적으로 인증되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 여행 상태 오류 또는 위치 불일치 오류
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 기타 예상치 못한 오류
            return ResponseEntity.internalServerError().body("여행 인증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PatchMapping("/{tripId}")
    public ResponseEntity<Map<String, String>> updateTrip(@PathVariable Long tripId,
                                                          @RequestBody Map<String, String> request) {
        TripEntity trip = tripService.getTripById(tripId);

        String newTitle = request.get("title");
        LocalDate startDate = request.containsKey("startDate") ? LocalDate.parse(request.get("startDate")) : trip.getStartDate();
        LocalDate endDate = request.containsKey("endDate") ? LocalDate.parse(request.get("endDate")) : trip.getEndDate();

        tripService.updateTripInfo(trip, newTitle, startDate, endDate);

        return ResponseEntity.ok(Map.of("message", "여행 정보가 성공적으로 수정되었습니다."));
    }
}