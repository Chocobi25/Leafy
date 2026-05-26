package com.chocobi.leafy.trip.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
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
    @JoinColumn(name = "route_option_id", nullable = false)
    private TripRouteOptionEntity routeOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_trip_place_id", nullable = false)
    private TripPlaceEntity startTripPlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_trip_place_id", nullable = false)
    private TripPlaceEntity endTripPlace;

    @Column(name = "distance", nullable = false)
    private double distance;

    @Column(name = "duration", nullable = false)
    private int duration; // 소요 시간 (분)

    @Column(name = "carbon_emission", nullable = false)
    private double carbonEmission;
}
