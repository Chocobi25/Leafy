package com.chocobi.leafy.post.vo;

import com.chocobi.leafy.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostError implements ErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다.");

    private final HttpStatus status;
    private final String message;
}
