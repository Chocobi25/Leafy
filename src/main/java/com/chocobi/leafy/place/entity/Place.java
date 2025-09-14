package com.chocobi.leafy.place.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Place implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;             // 제목

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;       // 장소 설명

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Category category;        // 자연, 체험, 문화, 음식

    @Column(nullable = false)
    private String address;           // 주소

    @Enumerated(EnumType.STRING)
    private RegionGroup regionGroup;

    private String regionDetail;

    @Column(nullable = false)
    private double latitude;          // 위도

    @Column(nullable = false)
    private double longitude;         // 경도

    private String tel;               // 전화번호

    @Column(length = 2000)
    private String url;               // 홈페이지

    private String copyright;         // 저작권

    @Enumerated(EnumType.STRING)
    private PlaceSourceType sourceType;   //API, USER 구분

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;         // 이미지

    public void updateFrom(Place other) {
        this.title = other.title;
        this.description = other.description;
        this.category = other.category;
        this.address = other.address;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.tel = other.tel;
        this.url = other.url;
        this.copyright = other.copyright;
    }
}
