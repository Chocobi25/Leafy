package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.place.entity.RegionGroup;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
public class TripDTO {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private RegionGroup departure;
    private RegionGroup arrival;
    private double carbonSaved;
    private double carbonEmission;
    private TripStatus status;
    private LocalDateTime certificationAt;
    private Long userId;
    private List<TripPlaceResponse> tripPlaces;

    public static TripDTO fromEntity(Trip trip) {
        return new TripDTO(
                trip.getId(),
                trip.getTitle(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDeparture(),
                trip.getArrival(),
                trip.getCarbonSaved(),
                trip.getCarbonEmission(),
                trip.getStatus(),
                trip.getCertificationAt(),
                trip.getUser().getId(),  // TODO: 로직 동작 확인
                trip.getTripPlaces().stream()
                        .map(TripPlaceResponse::toDTO)
                        .sorted(Comparator.comparingInt(TripPlaceResponse::getVisitOrder))
                        .toList()
        );
    }
}
