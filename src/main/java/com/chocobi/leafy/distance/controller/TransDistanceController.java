package com.chocobi.leafy.distance.controller;

import com.chocobi.leafy.distance.domain.DistanceResponse;
import com.chocobi.leafy.distance.service.TransDistanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class TransDistanceController {

    private final TransDistanceService transDistanceService;

    public TransDistanceController(TransDistanceService transDistanceService) {
        this.transDistanceService = transDistanceService;
    }

    @GetMapping("/transDistance")
    public DistanceResponse getDistance(@RequestParam String fromX, @RequestParam String fromY, @RequestParam String toX, @RequestParam String toY) {
        return transDistanceService.getDistance(fromX, fromY, toX, toY);
    }
}
