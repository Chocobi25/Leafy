package com.chocobi.leafy.place.infra.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "external_place_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImageEntity extends BaseEntity {
    @Column(length = 2000, nullable = false)
    private String url;

    private String source;

    private Integer sortOrder;

    private Boolean thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_place_id", nullable = false)
    private ExternalPlaceEntity place;

    @Builder
    public PlaceImageEntity(String url, String source, Integer sortOrder, Boolean thumbnail, ExternalPlaceEntity place) {
        this.url = url;
        this.source = source;
        this.sortOrder = sortOrder;
        this.thumbnail = thumbnail;
        this.place = place;
    }
}
