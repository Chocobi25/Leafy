package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSegmentRedisDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tripId;
    private Long startTripPlaceId;
    private Long endTripPlaceId;
    private String transport;
    private double distance;
    private int duration; // 소요 시간 (분)
    private double carbonEmission;

    public TripSegmentEntity toEntity(
            TripRouteOptionEntity routeOption,
            TripPlaceEntity startTripPlace,
            TripPlaceEntity endTripPlace
    ) {
        if (!startTripPlace.getTrip().getId().equals(endTripPlace.getTrip().getId())) {
            throw new IllegalArgumentException("출발/도착 여행지가 같은 여행에 속해야 합니다.");
        }
        if (!routeOption.getTrip().getId().equals(startTripPlace.getTrip().getId())) {
            throw new IllegalArgumentException("경로 후보와 여행지가 같은 여행에 속해야 합니다.");
        }

        return TripSegmentEntity.builder()
                .routeOption(routeOption)
                .startTripPlace(startTripPlace)
                .endTripPlace(endTripPlace)
                .distance(this.distance)
                .duration(this.duration)
                .carbonEmission(this.carbonEmission)
                .build();
    }
}
