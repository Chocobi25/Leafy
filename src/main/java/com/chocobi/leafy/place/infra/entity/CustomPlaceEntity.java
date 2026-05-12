package com.chocobi.leafy.place.infra.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_place")
@DiscriminatorValue("CUSTOM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomPlaceEntity extends PlaceEntity {
    @Builder
    public CustomPlaceEntity(String title, String address, double latitude, double longitude, String copyright) {
        super(title, address, latitude, longitude, copyright);
    }

    public void update(String title, String address, double latitude, double longitude, String copyright) {
        super.update(title, address, latitude, longitude, copyright);
    }
}
