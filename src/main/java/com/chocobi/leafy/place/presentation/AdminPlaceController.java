package com.chocobi.leafy.place.presentation;

import com.chocobi.leafy.place.application.AdminPlaceService;
import com.chocobi.leafy.place.dto.request.AdminCreatePlaceRequest;
import com.chocobi.leafy.place.dto.request.AdminUpdateCustomPlaceRequest;
import com.chocobi.leafy.place.dto.request.AdminUpdateExternalPlaceRequest;
import com.chocobi.leafy.place.dto.response.AdminPlaceDetailResponse;
import com.chocobi.leafy.place.dto.response.AdminPlaceListResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/v1/places")
@RequiredArgsConstructor
public class AdminPlaceController implements AdminPlaceDocs {
    private final AdminPlaceService adminPlaceService;

    @GetMapping
    @Override
    public ResponseEntity<List<AdminPlaceListResponse>> getPlaces() {
        List<AdminPlaceListResponse> places = adminPlaceService.getPlaces();
        return ResponseEntity.ok(places);
    }

    @GetMapping("/{placeId}")
    @Override
    public ResponseEntity<AdminPlaceDetailResponse> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(adminPlaceService.getPlace(placeId));
    }

    @PostMapping
    @Override
    public ResponseEntity<Long> createPlace(@Valid @RequestBody AdminCreatePlaceRequest request) {
        return ResponseEntity.ok(adminPlaceService.createPlace(request));
    }

    @PutMapping("/external")
    @Override
    public ResponseEntity<Void> updateExternalPlace(@Valid @RequestBody AdminUpdateExternalPlaceRequest request) {
        adminPlaceService.updateExternalPlace(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/custom")
    @Override
    public ResponseEntity<Void> updateCustomPlace(@Valid @RequestBody AdminUpdateCustomPlaceRequest request) {
        adminPlaceService.updateCustomPlace(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{placeId}")
    @Override
    public ResponseEntity<Void> deletePlace(@PathVariable Long placeId) {
        adminPlaceService.deletePlace(placeId);
        return ResponseEntity.noContent().build();
    }
}
