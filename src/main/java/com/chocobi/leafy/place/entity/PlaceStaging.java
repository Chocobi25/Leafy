package com.chocobi.leafy.place.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PlaceStaging {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Category category;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    private RegionGroup regionGroup;

    private String regionDetail;

    private Double latitude;
    private Double longitude;

    private String tel;

    @Column(length = 2000)
    private String url;

    private String copyright;

    // 어떤 API에서 왔는지 구분하기 위한 필드
    private String sourceApiName;
}
