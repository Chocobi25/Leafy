package com.chocobi.leafy.config;

import com.chocobi.leafy.user.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 0. 요청 경로 확인
        logger.info("Request to: {}", request.getRequestURI());

        // 1. 요청 헤더에서 "Authorization" 헤더를 찾음
        String authorizationHeader = request.getHeader("Authorization");

        // 2. 헤더가 없거나 "Bearer "로 시작하지 않으면 필터를 통과시킴 (인증 불필요)
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header is missing or does not start with Bearer.");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " 부분을 제거하여 실제 토큰만 추출
        String token = authorizationHeader.substring(7);
        logger.info("Extracted Token: {}", token);

        // 4. 토큰 유효성 검사
        if (jwtUtil.validateToken(token)) {
            logger.info("Token is valid.");
            // 5. 토큰이 유효하면 사용자 ID를 추출
            Long userId = jwtUtil.getUserIdFromToken(token);
            logger.info("User ID from token: {}", userId);

            // 6. Spring Security가 이해할 수 있는 인증 토큰(Authentication) 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userId, // Principal (주체)로 사용자 ID를 저장
                    null,   // Credentials (자격 증명)은 필요 없음
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 권한 설정
            );

            // 7. SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.info("Authentication successful. SecurityContext updated.");
        } else {
            logger.warn("Token is invalid.");
        }

        // 8. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}