package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.trip.infra.entity.TripPlace;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TripPlaceResponse {
    private Long tripId;
    private PlaceDTO place;
    private int visitOrder;
    private int day_index;
    private String memo;

    public static TripPlaceResponse toDTO(TripPlace tripPlace) {
        return TripPlaceResponse.builder()
                .tripId(tripPlace.getTrip().getId())
                .place(PlaceDTO.fromEntity(tripPlace.getPlace()))
                .visitOrder(tripPlace.getVisitOrder())
                .day_index(tripPlace.getDayIndex())
                .memo(tripPlace.getMemo())
                .build();
    }
}
