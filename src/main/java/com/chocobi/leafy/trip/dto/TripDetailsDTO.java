package com.chocobi.leafy.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TripDetailsDTO {
    private TripDTO trip;
    private List<TripSegmentDTO> tripSegments;
}
