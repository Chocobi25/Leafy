package com.chocobi.leafy.trip.entity;

import com.chocobi.leafy.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class TripSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "start_place_id")
    private Place startPlaceId;

    @ManyToOne
    @JoinColumn(name = "end_place_id")
    private Place endPlaceId;

    private String transport;

    private double distance;

    @Column(name = "carbon_emitted")
    private double carbonEmitted;

    @Column(name = "carbon_saved")
    private double carbonSaved;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * data insert 직전에 실행되는 어노테이션
     */
    @PrePersist
    public void creatTime() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
