package com.chocobi.leafy.auth.handler;

import com.chocobi.leafy.auth.service.RefreshTokenService;
import com.chocobi.leafy.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.redirect-uri}")
    private String redirectUri;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        Long userId = (Long) defaultOAuth2User.getAttributes().get("userId");
        String role = defaultOAuth2User.getAuthorities().iterator().next().getAuthority();
        String accessToken = jwtUtil.createAccessToken(userId, role);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        // 기존 Refresh Token 삭제 & 새로 발급한 Refresh Token 등록
        refreshTokenService.rotateRefreshToken(userId, refreshToken, jwtUtil.getRefreshTokenExpiration());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(14))
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        String redirectUrl = String.format("%s?accessToken=%s&userId=%s", redirectUri, accessToken, userId);

        response.sendRedirect(redirectUrl);
    }
}
