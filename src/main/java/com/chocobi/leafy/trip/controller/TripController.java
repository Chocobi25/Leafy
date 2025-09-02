package com.chocobi.leafy.trip.controller;

import com.chocobi.leafy.trip.dto.TripRequest;
import com.chocobi.leafy.trip.dto.TripPlaceListRequest;
import com.chocobi.leafy.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/api/trip")
    public Long saveTrip(@RequestBody TripRequest tripRequest, Authentication authentication) {
        Long kakaoId = (Long) authentication.getPrincipal(); // 사용자 ID 가져옴
        return tripService.createTrip(tripRequest, kakaoId);
    }

    @PostMapping("/api/trip/places")
    public ResponseEntity<Map<String, String>> saveTripPlaces(@RequestBody TripPlaceListRequest tripPlaceListRequest) {
        tripService.saveTripPlace(tripPlaceListRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "여행지가 성공적으로 저장되었습니다.");
        return ResponseEntity.ok(response);
    }
}
