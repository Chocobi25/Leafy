package com.chocobi.leafy.trip.vo;

import com.chocobi.leafy.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripError implements ErrorCode {
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 여행입니다."),
    TRIP_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 여행 장소입니다."),
    TRIP_SEGMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 여행 경로입니다.");

    private final HttpStatus status;
    private final String message;
}
