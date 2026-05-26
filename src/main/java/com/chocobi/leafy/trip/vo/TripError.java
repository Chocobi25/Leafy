package com.chocobi.leafy.trip.vo;

import com.chocobi.leafy.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripError implements ErrorCode {
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 여행입니다."),
    TRIP_SEGMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 여행 경로입니다."),
    INVALID_TRIP_REQUEST(HttpStatus.BAD_REQUEST, "여행 요청 정보가 올바르지 않습니다."),
    INVALID_TRIP_TITLE(HttpStatus.BAD_REQUEST, "여행 제목을 입력해주세요."),
    INVALID_TRIP_DATE(HttpStatus.BAD_REQUEST, "여행 날짜가 올바르지 않습니다."),
    INVALID_TRIP_REGION(HttpStatus.BAD_REQUEST, "여행 지역 정보가 올바르지 않습니다."),
    TRIP_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "진행 중인 여행만 인증할 수 있습니다."),
    TRIP_ALREADY_CERTIFIED(HttpStatus.BAD_REQUEST, "이미 위치 인증을 완료했습니다."),
    TRIP_LOCATION_UNAVAILABLE(HttpStatus.BAD_REQUEST, "현재 위치의 주소 정보를 가져올 수 없습니다. 다시 시도해주세요."),
    TRIP_LOCATION_MISMATCH(HttpStatus.BAD_REQUEST, "현재 위치가 여행 도착 지역과 다릅니다. 위치를 다시 확인해주세요."),
    TRIP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 여행에 접근할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
