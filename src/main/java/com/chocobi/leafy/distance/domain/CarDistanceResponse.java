package com.chocobi.leafy.distance.domain;

import lombok.Getter;

@Getter
public class CarDistanceResponse {

    private int distance; // 단위: 미터
    private int duration; // 단위: 밀리초

    public CarDistanceResponse(int distance, int duration) {
        this.distance = distance;
        this.duration = duration;
    }
}
