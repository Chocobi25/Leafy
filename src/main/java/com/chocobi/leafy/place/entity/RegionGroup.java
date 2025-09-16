package com.chocobi.leafy.place.entity;

import lombok.Getter;

import java.util.Map;

@Getter
public enum RegionGroup {
    SEOUL("서울"),
    INCHEON("인천"),
    BUSAN("부산"),
    DAEGU("대구"),
    DAEJEON("대전"),
    GWANGJU("광주"),
    ULSAN("울산"),
    SEJONG("세종특별자치시"),
    GYEONGGI("경기도"),
    GANGWON("강원특별자치도"),
    CHUNGBUK("충북"),
    CHUNGNAM("충남"),
    JEONBUK("전북특별자치도"),
    JEONNAM("전남"),
    GYEONGBUK("경북"),
    GYEONGNAM("경남"),
    JEJU("제주특별자치도");

    private final String koreanName;

    RegionGroup(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    // ------------------------------
    // 카카오 region_1depth_name → Enum 매핑
    // ------------------------------
    private static final Map<String, RegionGroup> REGION_MAP = Map.ofEntries(
            Map.entry("서울", SEOUL),
            Map.entry("인천", INCHEON),
            Map.entry("부산", BUSAN),
            Map.entry("대구", DAEGU),
            Map.entry("대전", DAEJEON),
            Map.entry("광주", GWANGJU),
            Map.entry("울산", ULSAN),
            Map.entry("세종특별자치시", SEJONG),
            Map.entry("경기", GYEONGGI),
            Map.entry("강원특별자치도", GANGWON),
            Map.entry("충북", CHUNGBUK),
            Map.entry("충남", CHUNGNAM),
            Map.entry("전북특별자치도", JEONBUK),
            Map.entry("전남", JEONNAM),
            Map.entry("경북", GYEONGBUK),
            Map.entry("경남", GYEONGNAM),
            Map.entry("제주특별자치도", JEJU)
    );

    public static RegionGroup fromRegionName(String region1DepthName) {
        return REGION_MAP.get(region1DepthName);
    }
}
