package com.chocobi.leafy.distance.controller;

import com.chocobi.leafy.distance.domain.CarDistanceRequest;
import com.chocobi.leafy.distance.domain.TransDistanceRequest;
import com.chocobi.leafy.distance.dto.RouteCalculationResult;
import com.chocobi.leafy.distance.dto.TmapResponse;
import com.chocobi.leafy.distance.service.CarDistanceService;
import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.service.TransDistanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/distance")
public class DistanceController {

    private final CarDistanceService carDistanceService;
    private final TransDistanceService transDistanceService;

    public DistanceController(CarDistanceService distanceService, TransDistanceService transDistanceService) {
        this.carDistanceService = distanceService;
        this.transDistanceService = transDistanceService;
    }

    @GetMapping("/car")
    public DistanceResponse getCarDistance(@RequestBody CarDistanceRequest request) {
        return carDistanceService.getDistance(request);
    }

    @GetMapping("/trans")
    public List<RouteCalculationResult> getTransDistance(@RequestBody TransDistanceRequest request) {
        return transDistanceService.getDistance(request);
    }
}
