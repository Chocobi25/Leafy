package com.chocobi.leafy.config;

import com.chocobi.leafy.constants.Kakao;
import com.chocobi.leafy.user.service.OAuth2UserService;
import com.chocobi.leafy.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.util.Arrays;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // CSRF 보호 비활성화

        // CORS 설정 추가
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

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

                // 프론트엔드 콜백 페이지로 리다이렉트
                String redirectUrl = String.format("%s?token=%s&userId=%s",
                        Kakao.redirectUri, token, userId);

                response.sendRedirect(redirectUrl);

//                // 응답 설정
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//
//                // 응답 본문에 담을 데이터 생성
//                Map<String, Object> responseBody = new HashMap<>();
//                responseBody.put("message", "Login successful");
//                responseBody.put("token", token);
//                responseBody.put("userId", userId);
//
//                // JSON으로 변환하여 응답 본문에 쓰기
//                PrintWriter writer = response.getWriter();
//                writer.print(objectMapper.writeValueAsString(responseBody));
//                writer.flush();
            } catch (Exception e) {
                // 예외 발생 시 에러와 함께 콜백 페이지로 리다이렉트
                e.printStackTrace();

                try {
                    String errorMessage = URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", "UTF-8");
                    String errorRedirectUrl = Kakao.redirectUri + "?error=" + errorMessage;
                    response.sendRedirect(errorRedirectUrl);
                } catch (Exception redirectException) {
                    redirectException.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        configuration.setAllowedOrigins(Arrays.asList(
                Kakao.clientUri
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
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