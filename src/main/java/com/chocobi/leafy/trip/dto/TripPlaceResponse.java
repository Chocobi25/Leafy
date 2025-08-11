package com.chocobi.leafy.trip.dto;

import com.chocobi.leafy.trip.entity.TripPlace;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TripPlaceResponse {
    private Long tripId;
    private Long placeId;
    private LocalDate visitDate;
    private int visitOrder;

    public static TripPlaceResponse toDTO(TripPlace tripPlace) {
        return TripPlaceResponse.builder()
                .tripId(tripPlace.getTrip().getId()) // Trip 엔티티에서 ID를 가져옴
                .placeId(tripPlace.getPlace().getId()) // Place 엔티티에서 이름을 가져옴
                .visitDate(tripPlace.getVisitDate())
                .visitOrder(tripPlace.getVisit_order())
                .build();
    }
}
