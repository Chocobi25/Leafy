package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
public class TripDTO {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private double carbonSaved;
    private double carbonEmission;
    private User user;
    private List<TripPlaceResponse> tripPlaces;

    public static TripDTO fromEntity(Trip trip) {
        return new TripDTO(
                trip.getId(),
                trip.getTitle(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getCarbonSaved(),
                trip.getCarbonEmission(),
                trip.getUser(),
                trip.getTripPlaces().stream()
                        .map(TripPlaceResponse::toDTO)
                        .sorted(Comparator.comparingInt(TripPlaceResponse::getVisitOrder))
                        .toList()
        );
    }
}
