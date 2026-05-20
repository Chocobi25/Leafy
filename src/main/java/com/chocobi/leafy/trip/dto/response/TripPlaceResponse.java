package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TripPlaceResponse {
    private Long tripPlaceId;
    private Long tripId;
    private PlaceDTO place;
    private int visitOrder;
    private int dayIndex;
    private String memo;

    public static TripPlaceResponse from(TripPlaceEntity tripPlace) {
        return TripPlaceResponse.builder()
                .tripPlaceId(tripPlace.getId())
                .tripId(tripPlace.getTrip().getId())
                .place(PlaceDTO.fromEntity(tripPlace.getPlace()))
                .visitOrder(tripPlace.getVisitOrder())
                .dayIndex(tripPlace.getDayIndex())
                .memo(tripPlace.getMemo())
                .build();
    }
}
