package com.chocobi.leafy.global.exception;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("커스텀 예외 발생: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(GlobalError.INVALID_INPUT_VALUE.getMessage());

        log.error("입력값 유효성 검사 실패: {}", message);
        return ResponseEntity
                .status(GlobalError.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.builder()
                        .status(GlobalError.INVALID_INPUT_VALUE.getStatus().value())
                        .code(GlobalError.INVALID_INPUT_VALUE.getCode())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(GlobalError.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(GlobalError.INTERNAL_SERVER_ERROR));
    }
}
