package com.chocobi.leafy.auth.handler;

import com.chocobi.leafy.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.frontend.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        Long userId = (Long) defaultOAuth2User.getAttributes().get("userId");
        String role = defaultOAuth2User.getAuthorities().iterator().next().getAuthority();
        String token = jwtUtil.createToken(userId, role);
        String redirectUrl = String.format("%s?token=%s&userId=%s", redirectUri, token, userId);

        response.sendRedirect(redirectUrl);
    }
}
