package com.chocobi.leafy.user.dto;

import com.chocobi.leafy.trip.infra.entity.TripStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserTripDto {
    private Long tripId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private double carbonSaved;
    private double carbonEmission;
    private TripStatus status;
    private LocalDateTime createdAt;
}