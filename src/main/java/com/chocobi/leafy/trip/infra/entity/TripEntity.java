package com.chocobi.leafy.trip.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "trip")
public class TripEntity extends BaseEntity {

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_region_id", nullable = false)
    private RegionEntity departure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_region_id", nullable = false)
    private RegionEntity arrival;

    @Column(name = "carbon_saved")
    @Builder.Default
    private double carbonSaved = 0.0;

    @Column(name = "carbon_emission")
    @Builder.Default
    private double carbonEmission = 0.0;

    @Column(name = "route_stale", nullable = false)
    @Builder.Default
    private boolean routeStale = false;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TripStatus status = TripStatus.CREATING;

    @Column(name = "certification_at")
    private LocalDateTime certificationAt;

    public void update(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void certify() {
        if (this.certificationAt != null) {
            throw new IllegalStateException("이미 위치 인증을 완료했습니다.");
        }
        this.certificationAt = LocalDateTime.now();
    }

    public void editStatus(TripStatus status) {
        this.status = status;
    }

    public void markRouteStale() {
        this.routeStale = true;
    }

    public void clearRouteStale() {
        this.routeStale = false;
    }
}
