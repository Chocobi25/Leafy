package com.chocobi.leafy.place.infra.entity;

import com.chocobi.leafy.global.entity.RegionEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "external_place")
@DiscriminatorValue("EXTERNAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalPlaceEntity extends PlaceEntity{
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    private RegionEntity region;

    private String tel;

    @Column(length = 2000)
    private String url;

    @Builder
    public ExternalPlaceEntity(String title, String address, double latitude, double longitude,
                               String copyright, RegionEntity region, String description,
                               CategoryEntity category, String tel, String url) {
        super(title, address, latitude, longitude, copyright);
        this.description = description;
        this.category = category;
        this.region = region;
        this.tel = tel;
        this.url = url;
    }

    public void update(ExternalPlaceEntity other) {
        super.update(other.getTitle(), other.getAddress(), other.getLatitude(), other.getLongitude(), other.getCopyright());
        this.description = other.description;
        this.category = other.category;
        this.region = other.region;
        this.tel = other.tel;
        this.url = other.url;
    }
}
