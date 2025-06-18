package com.chocobi.leafy.distance.controller;

import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.domain.CarDistanceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class CarDistanceController {

    private final CarDistanceService carDistanceService;

    public CarDistanceController(CarDistanceService distanceService) {
        this.carDistanceService = distanceService;
    }

    @GetMapping("/distance")
    public CarDistanceResponse getDistance(@RequestParam String from, @RequestParam String to) {
        return carDistanceService.getDistance(from, to);
    }
}
