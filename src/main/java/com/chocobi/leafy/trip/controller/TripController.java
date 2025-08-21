package com.chocobi.leafy.trip.controller;

import com.chocobi.leafy.trip.dto.TripRequest;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class TripController {
    private final TripService tripService;

    // Test용 메서드(수정할 예정)
    @PostMapping("/api/trip")
    public ResponseEntity<Trip> createTrip(@RequestBody TripRequest tripRequest) {
        // 여행 생성
        Long createdTripId = tripService.createTrip(tripRequest);

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
