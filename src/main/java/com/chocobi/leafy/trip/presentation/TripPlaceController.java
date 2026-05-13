package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.trip.application.TripPlaceService;
import com.chocobi.leafy.trip.application.TripSegmentService;
import com.chocobi.leafy.trip.application.TripService;
import com.chocobi.leafy.trip.dto.request.RecalculateRoutesRequest;
import com.chocobi.leafy.trip.dto.request.TripPlacesListRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripPlaceController {

    private final TripService tripService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;

    @PostMapping("/places")
    public ResponseEntity<Map<String, String>> saveTripPlaces(@RequestBody TripPlacesListRequest request) {
        TripEntity trip = tripService.getTripById(request.getTripId());
        tripPlaceService.saveInitialTripPlaces(trip, request);

        return ResponseEntity.ok(Map.of("message", "여행지가 성공적으로 저장되었습니다."));
    }

    @PatchMapping("/places")
    public ResponseEntity<Map<String, String>> updateTripPlaceDetails(@RequestBody TripPlacesListRequest request) {
        TripEntity trip = tripService.getTripById(request.getTripId());
        tripPlaceService.editTripPlaceDetails(trip, request.getPlaces());

        return ResponseEntity.ok(Map.of("message", "여행지 정보가 성공적으로 업데이트되었습니다."));
    }

    @PatchMapping("/edit")
    public ResponseEntity<Map<String, String>> editTripPlaceDetails(@RequestBody RecalculateRoutesRequest request) {
        TripEntity trip = tripService.getTripById(request.getTripId());

        tripPlaceService.editTripPlaceDetails(trip, request.getPlaces());
        List<TripPlaceResponse> updatedTripPlaces = tripPlaceService.getTripPlaces(trip.getId());
        tripSegmentService.recalculateRoutesAndSaveV2(trip, request.getTransport(), updatedTripPlaces);
        tripSegmentService.completeTripSegments(trip.getId(), request.getTransport());

        return ResponseEntity.ok(Map.of("message", "여행지 정보 및 경로가 성공적으로 업데이트되었습니다."));
    }
}
