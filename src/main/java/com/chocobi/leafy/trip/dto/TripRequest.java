package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.place.entity.RegionGroup;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TripRequest {
    private String title;
    private LocalDate start_date;
    private LocalDate end_date;
    private String departure;
    private String arrival;
}
