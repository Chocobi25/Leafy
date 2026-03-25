package com.chocobi.leafy.config;

import com.chocobi.leafy.auth.filter.JwtAuthenticationFilter;
import com.chocobi.leafy.auth.handler.OAuth2SuccessHandler;
import com.chocobi.leafy.auth.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // JwtAuthenticationFilter 주입
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Value("${app.frontend.client-uri}")
    private String clientUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // CSRF 보호 비활성화

        // CORS 설정 추가
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // JWT를 사용하므로 세션을 STATELESS로 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 로그인 설정
        http.oauth2Login(oauth2Configurer -> oauth2Configurer
                .userInfoEndpoint(endpoint -> endpoint.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler));

        // API 경로에 대한 접근 권한 설정 (예시: /api/** 경로는 인증 필요)
        http.authorizeHttpRequests(config -> config
                // 공개 API - 인증 불필요
                .requestMatchers("/api/place/list").permitAll()  // 지역별 장소 조회
                .requestMatchers("/api/place/api-places").permitAll()  // API 장소 목록
                .requestMatchers("/api/place/user-place").authenticated()  // 사용자 장소 등록 (인증 필요)

                // 관리자 전용 API - ADMIN 권한 필요
                .requestMatchers("/api/place/all").hasRole("ADMIN")  // 모든 장소 조회
                .requestMatchers("/api/place/{placeId}").hasRole("ADMIN")  // 장소 삭제
                .requestMatchers("/api/place/image/{imageId}").hasRole("ADMIN")  // 이미지 삭제

                // 포스트 관련
                .requestMatchers("/api/posts").permitAll()
                .requestMatchers("/api/posts/likes/me").permitAll()

                // RefreshToken 재발급
                .requestMatchers("/api/auth/refresh").permitAll()

                // 나머지 /api/** 경로는 인증 필요
                .requestMatchers("/api/**").authenticated()

                // 그 외 모든 요청은 허용
                .anyRequest().permitAll());

        // 직접 만든 JWT 필터를 Spring Security 필터 체인에 추가
        // UsernamePasswordAuthenticationFilter 이전에 실행되도록 설정
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        configuration.setAllowedOrigins(Arrays.asList(
                clientUri
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 쿠키/인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}