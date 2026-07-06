package com.chocobi.leafy.trip.vo;

import com.chocobi.leafy.global.exception.CustomException;

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
            throw new CustomException(TripError.INVALID_TRIP_TRANSPORT);
        }

        String normalized = transport.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "car", "자동차" -> CAR;
            case "public", "public_trans", "bus", "대중교통" -> PUBLIC;
            default -> throw new CustomException(TripError.INVALID_TRIP_TRANSPORT);
        };
    }
}
