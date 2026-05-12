package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.trip.infra.entity.TripSegment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripSegmentDTO {
    private Long id;
    private Long tripId;
    private Long startPlaceId;
    private Long endPlaceId;
    private String transport;
    private double distance;
    private int duration;
    private double carbonEmitted;
    private double maxCarbonEmission;

    public static TripSegmentDTO fromEntity(TripSegment tripSegment) {
        return new TripSegmentDTO(
                tripSegment.getId(),
                tripSegment.getTripId().getId(),
                tripSegment.getStartPlaceId().getId(),
                tripSegment.getEndPlaceId().getId(),
                tripSegment.getTransport(),
                tripSegment.getDistance(),
                tripSegment.getDuration(),
                tripSegment.getCarbonEmitted(),
                tripSegment.getMaxCarbonEmission()
        );
    }
}
