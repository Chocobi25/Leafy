package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
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
    private Long startPlaceId;
    private Long endPlaceId;
    private String transport;
    private double distance;
    private int duration; // 소요 시간 (분)
    private double carbonEmitted;
    private double maxCarbonEmission;

    public TripSegmentEntity toEntity(TripEntity trip, PlaceEntity startPlace, PlaceEntity endPlace) {
        return TripSegmentEntity.builder()
                .trip(trip)
                .startPlace(startPlace)
                .endPlace(endPlace)
                .transport(this.transport)
                .distance(this.distance)
                .duration(this.duration)
                .carbonEmitted(this.carbonEmitted)
                .maxCarbonEmission(this.maxCarbonEmission)
                .build();
    }
}
