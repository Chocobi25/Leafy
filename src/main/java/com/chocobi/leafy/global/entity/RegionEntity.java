package com.chocobi.leafy.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RegionEntity parent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegionLevel level;

    public RegionEntity(String name, RegionEntity parent, RegionLevel level) {
        this.name = name;
        this.parent = parent;
        this.level = level;
    }
}
