package com.chocobi.leafy.trip.dto.response;

import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TripPlaceResponse {
    private Long tripPlaceId;
    private Long tripId;
    private TripPlaceLocationResponse place;
    private int visitOrder;
    private int dayIndex;
    private String memo;

    public static TripPlaceResponse from(TripPlaceEntity tripPlace) {
        return TripPlaceResponse.builder()
                .tripPlaceId(tripPlace.getId())
                .tripId(tripPlace.getTrip().getId())
                .place(TripPlaceLocationResponse.from(tripPlace.getPlace()))
                .visitOrder(tripPlace.getVisitOrder())
                .dayIndex(tripPlace.getDayIndex())
                .memo(tripPlace.getMemo())
                .build();
    }
}
