package com.chocobi.leafy.place.controller;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.place.common.dto.UserPlaceDTO;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/list")
    public ResponseEntity<List<PlaceDTO>> getPlacesByArrival(@RequestParam String arrival) {
        List<PlaceDTO> places = placeService.getPlacesByArrival(arrival);
        return ResponseEntity.ok(places);
    }

    @PostMapping("/user-place")
    public ResponseEntity<Map<String, Long>> saveUserPlace(@RequestBody UserPlaceDTO userPlaceDTO) {
        Long id = placeService.saveUserPlace(userPlaceDTO);
        return ResponseEntity.ok(Map.of("kakaoPlace_id", id));
    }

    @GetMapping("/api-places")
    public ResponseEntity<List<PlaceDTO>> getPlacesByApi() {
        List<PlaceDTO> places = placeService.getPlaceBySourceType(PlaceSourceType.API);
        return ResponseEntity.ok(places);
    }

    @DeleteMapping("/{placeId}")
    @PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<Map<String, String>> deletePlace(@PathVariable Long placeId) {
        try {
            placeService.deletePlace(placeId);
            return ResponseEntity.ok(Map.of("message", "장소가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/image/{imageId}")
    @PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long imageId) {
        try {
            placeService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of("message", "이미지가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<List<PlaceDTO>> getAllPlaces() {
        List<PlaceDTO> places = placeService.getAllPlaces();
        return ResponseEntity.ok(places);
    }
}
