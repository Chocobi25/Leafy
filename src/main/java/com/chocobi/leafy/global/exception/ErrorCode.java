package com.chocobi.leafy.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();
    String getMessage();

    default String getCode() {
        if (this instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        return getClass().getSimpleName();
    }
}
