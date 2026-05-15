package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.trip.application.TripPlaceService;
import com.chocobi.leafy.trip.application.TripPlaceRouteService;
import com.chocobi.leafy.trip.dto.request.RecalculateRoutesRequest;
import com.chocobi.leafy.trip.dto.request.TripPlacesListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;
    private final TripPlaceRouteService tripPlaceRouteService;

    @PostMapping("/places")
    public ResponseEntity<Map<String, String>> saveTripPlaces(
            @RequestBody TripPlacesListRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        tripPlaceService.saveInitialTripPlaces(request, userId);

        return ResponseEntity.ok(Map.of("message", "여행지가 성공적으로 저장되었습니다."));
    }

    @PatchMapping("/places")
    public ResponseEntity<Map<String, String>> updateTripPlaceDetails(
            @RequestBody TripPlacesListRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        tripPlaceService.editTripPlaceDetails(request, userId);

        return ResponseEntity.ok(Map.of("message", "여행지 정보가 성공적으로 업데이트되었습니다."));
    }

    @PatchMapping("/edit")
    public ResponseEntity<Map<String, String>> editTripPlaceDetails(
            @RequestBody RecalculateRoutesRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        tripPlaceRouteService.editTripPlacesAndRecalculateRoutes(request, userId);

        return ResponseEntity.ok(Map.of("message", "여행지 정보 및 경로가 성공적으로 업데이트되었습니다."));
    }
}
