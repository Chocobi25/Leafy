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

        logger.info("Request to: {}", request.getRequestURI());

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header is missing or does not start with Bearer.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);
        logger.info("Extracted Token: {}", token);

        if (jwtUtil.validateToken(token)) {
            logger.info("Token is valid.");

            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token); // Role 정보 추출

            logger.info("User ID from token: {}, Role: {}", userId, role);

            // Role에 따라 권한 설정
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role)) // JWT에서 추출한 Role 사용
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.info("Authentication successful. SecurityContext updated.");
        } else {
            logger.warn("Token is invalid.");
        }

        filterChain.doFilter(request, response);
    }
}