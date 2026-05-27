package com.chocobi.leafy.trip.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import com.chocobi.leafy.trip.vo.TripTransport;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "trip_route_option")
public class TripRouteOptionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "transport", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TripTransport transport;

    @Column(name = "total_distance", nullable = false)
    private double totalDistance;

    @Column(name = "total_duration", nullable = false)
    private int totalDuration;

    @Column(name = "total_carbon_emission", nullable = false)
    private double totalCarbonEmission;

    @Column(name = "recommended", nullable = false)
    @Builder.Default
    private boolean recommended = false;

    @Column(name = "confirmed", nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    public void recommend() {
        this.recommended = true;
    }

    public void unrecommend() {
        this.recommended = false;
    }

    public void confirm() {
        this.confirmed = true;
    }

    public void unconfirm() {
        this.confirmed = false;
    }
}
