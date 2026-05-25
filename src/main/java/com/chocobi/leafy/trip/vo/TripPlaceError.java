package com.chocobi.leafy.trip.vo;

import com.chocobi.leafy.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripPlaceError implements ErrorCode {
    TRIP_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 여행 장소입니다."),
    TRIP_PLACES_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 여행 장소가 존재합니다."),
    TRIP_PLACES_NOT_CREATED(HttpStatus.BAD_REQUEST, "여행 장소가 아직 생성되지 않았습니다."),
    DUPLICATE_TRIP_PLACE_REQUEST(HttpStatus.BAD_REQUEST, "중복된 여행 장소 요청입니다."),
    INVALID_TRIP_PLACE_REQUEST(HttpStatus.BAD_REQUEST, "여행 장소 요청 정보가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
