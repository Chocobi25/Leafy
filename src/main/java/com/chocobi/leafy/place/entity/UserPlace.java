package com.chocobi.leafy.place.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private String place_url;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Type type;

    @Builder
    public UserPlace(String title, String address, double latitude, double longitude, String place_url, Type type) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.place_url = place_url;
        this.type = type;
    }
}
