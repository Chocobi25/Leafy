package com.chocobi.leafy.trip.dto;

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
