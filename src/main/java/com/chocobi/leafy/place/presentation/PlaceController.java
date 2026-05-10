package com.chocobi.leafy.place.presentation;

import com.chocobi.leafy.place.application.*;
import com.chocobi.leafy.place.dto.response.ExternalPlaceDetailResponse;
import com.chocobi.leafy.place.dto.response.ExternalPlaceListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController implements PlaceDocs {
    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<List<ExternalPlaceListResponse>> getPlaces() {
        List<ExternalPlaceListResponse> places = placeService.getExternalPlaces();
        return ResponseEntity.ok(places);
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<ExternalPlaceDetailResponse> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(placeService.getExternalPlace(placeId));
    }
}
