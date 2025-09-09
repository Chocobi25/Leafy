package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripSegment;
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

    public TripSegment toEntity() {
        return TripSegment.builder()
                .tripId(Trip.builder().id(this.tripId).build())
                .startPlaceId(Place.builder().id(this.startPlaceId).build())
                .endPlaceId(Place.builder().id(this.endPlaceId).build())
                .transport(this.transport)
                .distance(this.distance)
                .duration(this.duration)
                .carbonEmitted(this.carbonEmitted)
                .build();
    }
}
