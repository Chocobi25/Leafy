package com.chocobi.leafy.constants;

public class DistanceConst {
    public static final String kakaoUri = "/v1/waypoints/directions";
    public static final String tmapUri = "/transit/routes";

    public static final double ROAD_CORRECTION_FACTOR = 1.3; // 직선거리 -> 도로 거리 보정계수
    public static final double AVERAGE_CAR_SPEED_MPS = 50 * 1000.0 / 3600.0; // 평균 자동차 속도 50km/h -> m/s
}
