package com.chocobi.leafy.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
