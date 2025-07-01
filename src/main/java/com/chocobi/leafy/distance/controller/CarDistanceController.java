package com.chocobi.leafy.distance.controller;

import com.chocobi.leafy.distance.domain.DistanceRequest;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class CarDistanceController {

    private final CarDistanceService carDistanceService;

    public CarDistanceController(CarDistanceService distanceService) {
        this.carDistanceService = distanceService;
    }

    @GetMapping("/CarDistance")
    public DistanceResponse getDistance(@RequestBody DistanceRequest request) {
        return carDistanceService.getDistance(request);
    }
}
