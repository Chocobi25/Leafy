package com.chocobi.leafy.config;

import com.chocobi.leafy.user.service.OAuth2UserService;
import com.chocobi.leafy.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // JwtAuthenticationFilter 주입

    // 생성자 수정
    public SecurityConfig(OAuth2UserService oAuth2UserService, JwtUtil jwtUtil, ObjectMapper objectMapper, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.oAuth2UserService = oAuth2UserService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // CSRF 보호 비활성화

        // JWT를 사용하므로 세션을 STATELESS로 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 로그인 설정
        http.oauth2Login(oauth2Configurer -> oauth2Configurer
                .successHandler(successHandler()));

        // API 경로에 대한 접근 권한 설정 (예시: /api/** 경로는 인증 필요)
        http.authorizeHttpRequests(config -> config
                .requestMatchers("/api/**").authenticated() // /api/로 시작하는 모든 경로는 인증 필요
                .anyRequest().permitAll()); // 그 외 모든 요청은 허용

        // 직접 만든 JWT 필터를 Spring Security 필터 체인에 추가
        // UsernamePasswordAuthenticationFilter 이전에 실행되도록 설정
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> {
            try {
                // OAuth2 로그인 결과로 받은 사용자 정보
                DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

                // 카카오 ID를 PK로 사용하므로, attributes에서 "id"를 직접 추출
                Long userId = (Long) defaultOAuth2User.getAttributes().get("id");

                // 사용자의 권한 정보 (예: "ROLE_USER")
                String role = defaultOAuth2User.getAuthorities().iterator().next().getAuthority();

                // JWT 생성
                String token = jwtUtil.createToken(userId, role);

                // 응답 설정
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                // 응답 본문에 담을 데이터 생성
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", "Login successful");
                responseBody.put("token", token);
                responseBody.put("userId", userId);

                // JSON으로 변환하여 응답 본문에 쓰기
                PrintWriter writer = response.getWriter();
                writer.print(objectMapper.writeValueAsString(responseBody));
                writer.flush();
            } catch (Exception e) {
                // 예외 발생 시 로그 기록
                e.printStackTrace();

                // 프론트엔드에 에러 응답 전송
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                Map<String, Object> errorBody = new HashMap<>();
                errorBody.put("error", "Login failed due to an internal server error");
                errorBody.put("message", e.getMessage());

                PrintWriter writer = response.getWriter();
                writer.print(objectMapper.writeValueAsString(errorBody));
                writer.flush();
            }
        });
    }
}