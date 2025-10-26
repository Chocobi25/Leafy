package com.chocobi.leafy.place.entity;

import lombok.Getter;

import java.util.Map;

@Getter
public enum RegionGroup {
    SEOUL("서울", 37.5565, 126.9706),
    INCHEON("인천", 37.4563, 126.7052),
    BUSAN("부산", 35.1796, 129.0756),
    DAEGU("대구", 35.8714, 128.6014),
    DAEJEON("대전", 36.3504, 127.3845),
    GWANGJU("광주", 35.1595, 126.8853),
    ULSAN("울산", 35.5384, 129.3114),
    SEJONG("세종특별자치시", 36.4800, 127.2890),
    GYEONGGI("경기도", 37.4138, 127.5183),
    GANGWON("강원특별자치도", 37.7519, 128.8761),
    CHUNGBUK("충북", 36.7801, 127.6534),
    CHUNGNAM("충남", 36.5184, 126.8000),
    JEONBUK("전북특별자치도", 35.8242, 127.1480),
    JEONNAM("전남", 34.8679, 126.9910),
    GYEONGBUK("경북", 36.5760, 128.5050),
    GYEONGNAM("경남", 35.2598, 128.6647),
    JEJU("제주특별자치도", 33.4996, 126.5312);

    private final String koreanName;
    private final double latitude;
    private final double longitude;

    RegionGroup(String koreanName, double latitude, double longitude) {
        this.koreanName = koreanName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private static final Map<String, RegionGroup> REGION_MAP = Map.ofEntries(
            Map.entry("서울", SEOUL),
            Map.entry("인천", INCHEON),
            Map.entry("부산", BUSAN),
            Map.entry("대구", DAEGU),
            Map.entry("대전", DAEJEON),
            Map.entry("광주", GWANGJU),
            Map.entry("울산", ULSAN),
            Map.entry("세종특별자치시", SEJONG),
            Map.entry("세종", SEJONG),
            Map.entry("경기", GYEONGGI),
            Map.entry("강원특별자치도", GANGWON),
            Map.entry("강원", GANGWON),
            Map.entry("충북", CHUNGBUK),
            Map.entry("충남", CHUNGNAM),
            Map.entry("전북특별자치도", JEONBUK),
            Map.entry("전북", JEONBUK),
            Map.entry("전남", JEONNAM),
            Map.entry("경북", GYEONGBUK),
            Map.entry("경남", GYEONGNAM),
            Map.entry("제주특별자치도", JEJU),
            Map.entry("제주", JEJU)
    );

    public static RegionGroup fromRegionName(String region1DepthName) {
        try {
            return RegionGroup.valueOf(region1DepthName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return REGION_MAP.get(region1DepthName);
        }
    }
}
