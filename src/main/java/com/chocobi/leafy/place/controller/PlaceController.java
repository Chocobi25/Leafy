package com.chocobi.leafy.place.controller;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.place.common.dto.UserPlaceDTO;
import com.chocobi.leafy.place.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        List<PlaceDTO> places = placeService.getPlaceByAddress(arrival);
        return ResponseEntity.ok(places);
    }

    @PostMapping("/user-place")
    public ResponseEntity<Map<String, Long>> saveUserPlace(@RequestBody UserPlaceDTO userPlaceDTO) {
        Long id = placeService.saveUserPlace(userPlaceDTO);
        return ResponseEntity.ok(Map.of("kakaoPlace_id", id));
    }
}
