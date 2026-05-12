package com.chocobi.leafy.trip.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "trip_segment")
public class TripSegmentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    // TODO: TripPlace 연결 고려
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_place_id", nullable = false)
    private PlaceEntity startPlace;

    // TODO: TripPlace 연결 고려
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_place_id", nullable = false)
    private PlaceEntity endPlace;

    @Column(name = "transport", nullable = false, length = 50)
    private String transport;

    @Column(name = "distance", nullable = false)
    private double distance;

    @Column(name = "duration", nullable = false)
    private int duration; // 소요 시간 (분)

    @Column(name = "carbon_emitted", nullable = false)
    private double carbonEmitted;

    @Column(name = "max_carbon_emission", nullable = false)
    private double maxCarbonEmission;
}
