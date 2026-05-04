package com.chocobi.leafy.place.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;

import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "place_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PlaceEntity extends BaseEntity {
   @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private String copyright;

    protected PlaceEntity(String title, String address, double latitude, double longitude, String copyright) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.copyright = copyright;
    }

    public void update(String title, String address, double latitude, double longitude, String copyright) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.copyright = copyright;
    }
}
