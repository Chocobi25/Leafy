package com.chocobi.leafy.place.controller;

import com.chocobi.leafy.place.dto.PlaceDTO;
import com.chocobi.leafy.place.dto.UserPlaceDTO;
import com.chocobi.leafy.place.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PlaceController {
    private final EcoService ecoService;
    private final RuralService ruralService;
    private final FarmService farmService;
    private final ThemeService themeService;
    private final PlaceService placeService;

    @PostMapping("/api/place/eco")
    public ResponseEntity<Map<String, Object>> saveEco() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            ecoService.saveEcoPlace();
            result.put("ecoService", "OK");
        } catch (Exception e) {
            result.put("ecoService", "FAIL: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/place/rural")
    public ResponseEntity<Map<String, Object>> saveRural() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            ruralService.saveRuralPlace();
            result.put("ruralService", "OK");
        } catch (Exception e) {
            result.put("ruralService", "FAIL: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/place/farm")
    public ResponseEntity<Map<String, Object>> saveFarm() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            farmService.saveFarmPlace();
            result.put("farmService", "OK");
        } catch (Exception e) {
            result.put("farmService", "FAIL: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/place/theme")
    public ResponseEntity<Map<String, Object>> saveTheme() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            themeService.saveThemePlace();
            result.put("themeService", "OK");
        } catch (Exception e) {
            result.put("themeService", "FAIL: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/place/list")
    public ResponseEntity<List<PlaceDTO>> getPlacesByArrival(@RequestParam String arrival) {
        List<PlaceDTO> places = placeService.getPlaceByAddress(arrival);
        return ResponseEntity.ok(places);
    }

    @PostMapping("/api/place/user-place")
    public ResponseEntity<Map<String, Long>> saveUserPlace(@RequestParam UserPlaceDTO userPlaceDTO) {
        Long id = placeService.saveUserPlace(userPlaceDTO);
        return ResponseEntity.ok(Map.of("kakaoPlace_id", id));
    }

}
