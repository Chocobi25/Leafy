package com.chocobi.leafy.trip.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "trip_place")
public class TripPlaceEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    // TODO: TripPlace 연결 고려
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private PlaceEntity place;

    @Column(name = "day_index")
    @Builder.Default
    private int dayIndex = 0;

    @Column(name = "visit_order")
    @Builder.Default
    private int visitOrder = 0;

    @Size(max = 255)
    @Column(length = 255)
    private String memo;

    public void updateDetails(PlaceEntity place, Integer dayIndex, Integer visitOrder, String memo) {
        this.place = place;
        this.dayIndex = dayIndex;
        this.visitOrder = visitOrder;
        this.memo = memo;
    }

    public void updateSchedule(Integer dayIndex, Integer visitOrder, String memo) {
        this.dayIndex = dayIndex;
        this.visitOrder = visitOrder;
        this.memo = memo;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

}
