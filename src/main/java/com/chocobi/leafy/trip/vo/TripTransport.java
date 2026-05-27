package com.chocobi.leafy.trip.vo;

import java.util.Locale;

public enum TripTransport {
    CAR("car"),
    PUBLIC("public");

    private final String code;

    TripTransport(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static TripTransport from(String transport) {
        if (transport == null) {
            throw new IllegalArgumentException("transport가 필요합니다.");
        }

        String normalized = transport.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "car", "자동차" -> CAR;
            case "public", "public_trans", "bus", "대중교통" -> PUBLIC;
            default -> throw new IllegalArgumentException("지원하지 않는 교통수단입니다: " + transport);
        };
    }
}
