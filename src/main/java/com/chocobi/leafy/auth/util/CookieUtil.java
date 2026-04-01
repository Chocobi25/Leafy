package com.chocobi.leafy.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    public ResponseCookie createRefreshTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return createRefreshTokenCookie("", Duration.ZERO);
    }
}
