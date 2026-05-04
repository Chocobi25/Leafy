package com.chocobi.leafy.trip.entity;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TripPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    private PlaceEntity place;

    @Builder.Default
    private int dayIndex = 0;

    @Builder.Default
    private int visitOrder = 0;

    private String memo;

    public void updateDetails(int dayIndex, int visitOrder, String memo){
        this.dayIndex = dayIndex;
        this.visitOrder = visitOrder;
        this.memo = memo;
    }
}
