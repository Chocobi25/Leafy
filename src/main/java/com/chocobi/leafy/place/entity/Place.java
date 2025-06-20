package com.chocobi.leafy.place.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;       // 장소 설명

    @Enumerated(EnumType.STRING)
    private Category category;        // 자연, 체험, 문화, 음식

    @Column(nullable = false)
    private String address;           // 주소

    @Column(nullable = false)
    private double latitude;          // 위도

    @Column(nullable = false)
    private double longitude;         // 경도

    private String imageUrl;          // 이미지
    private String tel;               // 대표 전화번호
    private String copyright;         // 저작권
}
