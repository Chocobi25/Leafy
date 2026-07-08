package com.chocobi.leafy.trip.vo;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TripTransport {
    CAR("car"),
    PUBLIC("public");

    private final String code;

    TripTransport(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
