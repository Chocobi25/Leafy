package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.place.entity.Type;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TripPlaceRequest {
    private Long placeId;
    private Type placeType;
    private int visitOrder;
    private LocalDate visitDate;
}
