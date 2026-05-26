package com.chocobi.leafy.distance.service;

import com.chocobi.leafy.distance.domain.Point;
import com.chocobi.leafy.distance.domain.Port;
import com.chocobi.leafy.place.infra.entity.RegionGroup;
import com.chocobi.leafy.trip.dto.response.TripPlaceLocationResponse;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DistanceUtils {
    /**
     * 제주 여행 여부 판별
     */
    public static boolean isJejuTrip(List<TripPlaceResponse> tripPlaces) {
        return tripPlaces.stream()
                .anyMatch(tripPlace -> {
                    TripPlaceLocationResponse place = tripPlace.getPlace();
                    return place.getAddress() != null && place.getAddress().contains("제주");
                });
    }

    /**
     * 하버사인 공식으로 두 점 간 직선거리 계산
     */
    public static double calculateStraightDistance(Point start, Point end) {
        final double R = 6371000; // 지구 반지름 (m)
        double lat1Rad = Math.toRadians(start.getY());
        double lat2Rad = Math.toRadians(end.getY());
        double deltaLatRad = Math.toRadians(end.getY() - start.getY());
        double deltaLonRad = Math.toRadians(end.getX() - start.getX());

        double a = Math.sin(deltaLatRad/2) * Math.sin(deltaLatRad/2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad/2) * Math.sin(deltaLonRad/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    /**
     * 출발지 + 경유지 + 도착지를 합친 전체 경로 반환
     */
    public static List<Point> buildAllPoints(Point origin, Point destination, List<Point> waypoints) {
        List<Point> allPoints = new ArrayList<>();
        allPoints.add(origin);
        if (waypoints != null && !waypoints.isEmpty()) {
            allPoints.addAll(waypoints);
        }
        allPoints.add(destination);
        return allPoints;
    }

    /**
     * Place → Point 변환
     */
    public static Point placeToPoint(TripPlaceLocationResponse place) {
        return new Point(place.getLongitude(), place.getLatitude());
    }

    /**
     * Region → Point 변환
     */
    public static Point regionToPoint(RegionGroup region) {
        return new Point(region.getLongitude(), region.getLatitude());
    }

    /**
     * 대상 점에서 가장 가까운 항구 찾기
     */
    public static Port findNearestPort(Point target, List<Port> ports) {
        return ports.stream()
                .min(Comparator.comparingDouble(p ->
                        calculateStraightDistance(target, p.getPoint())
                ))
                .orElseThrow();
    }
}
