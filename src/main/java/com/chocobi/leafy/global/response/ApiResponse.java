package com.chocobi.leafy.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("요청이 성공했습니다.")
                .data(data)
                .build();
    }
}