package com.chocobi.leafy.fcm.vo;

import com.chocobi.leafy.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FcmError implements ErrorCode {
    FCM_NOTIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
