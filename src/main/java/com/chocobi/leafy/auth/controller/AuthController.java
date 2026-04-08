package com.chocobi.leafy.auth.controller;

import com.chocobi.leafy.auth.dto.TokenPair;
import com.chocobi.leafy.auth.service.AuthService;
import com.chocobi.leafy.auth.util.CookieUtil;
import com.chocobi.leafy.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<String>> refresh(
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        TokenPair tokenPair = authService.reissueAccessToken(refreshToken);

        ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(tokenPair.refreshToken(), Duration.ofDays(14));

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(SuccessResponse.of(tokenPair.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        authService.logout(refreshToken);

        ResponseCookie deleteCookie = cookieUtil.deleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie.toString())
                .build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Long userId
    ) {
        authService.withdraw(userId);

        ResponseCookie deleteCookie = cookieUtil.deleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie.toString())
                .build();
    }
}
