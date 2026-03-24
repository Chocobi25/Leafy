package com.chocobi.leafy.auth.filter;

import com.chocobi.leafy.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Request to: {}", request.getRequestURI());

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or does not start with Bearer.");
            filterChain.doFilter(request, response);  // 다음 필터 호출
            return;
        }

        String token = authorizationHeader.substring(7);
        log.info("Extracted Token: {}...", token.substring(0, Math.min(token.length(), 10)));

        if (jwtUtil.validateToken(token)) {
            log.info("Token is valid.");

            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token); // Role 정보 추출

            log.info("User ID from token: {}, Role: {}", userId, role);

            // Role에 따라 권한 설정
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role)) // JWT에서 추출한 Role 사용
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("Authentication successful. SecurityContext updated.");
        } else {
            log.warn("Token is invalid.");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }
}