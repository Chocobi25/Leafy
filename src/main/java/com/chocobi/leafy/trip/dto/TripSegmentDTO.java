package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
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
    private double carbonEmission;

    public static TripSegmentDTO fromEntity(TripSegmentEntity tripSegment) {
        return new TripSegmentDTO(
                tripSegment.getId(),
                tripSegment.getRouteOption().getTrip().getId(),
                tripSegment.getStartTripPlace().getPlace().getId(),
                tripSegment.getEndTripPlace().getPlace().getId(),
                tripSegment.getRouteOption().getTransport().getCode(),
                tripSegment.getDistance(),
                tripSegment.getDuration(),
                tripSegment.getCarbonEmission()
        );
    }
}
