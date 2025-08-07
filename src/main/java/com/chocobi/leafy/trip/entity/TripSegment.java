package com.chocobi.leafy.trip.entity;

import com.chocobi.leafy.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "trip_id")
//    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "start_place_id")
    private Place startPlaceId;

    @ManyToOne
    @JoinColumn(name = "end_place_id")
    private Place endPlaceId;

    private String transport;

    @Column(name = "carbon_emitted")
    private double carbonEmitted;

    @Column(name = "carbon_saved")
    private double carbonSaved;
}
