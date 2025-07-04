package com.chocobi.leafy.config;


import com.chocobi.leafy.user.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;

    public SecurityConfig(OAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // CSRF 보호 기능 비활성화. REST API나 외부 클라이언트에서 토큰 기반 인증을 주로 사용할 때 CSRF를 끄는 경우가 많음
        http.authorizeHttpRequests(config -> config.anyRequest().permitAll()); // 모든 요청에 대해 인증을 요구하지 않고 허용
        http.oauth2Login(oauth2Configurer -> oauth2Configurer // OAuth2 로그인 기능 활성화, 커스터마이징
                .loginPage("/login") // 로그인 페이지의 엔드포인트
                .successHandler(successHandler()) // 로그인 성공 시 커스텀 핸들러를 호출해 추가 로직 수행
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService))); // OAuth2 공급자로부터 받은 엑세스 토큰으로 사용자 정보를 조회할 때 사용할 서비스(oAuth2UserService를 지정

        return http.build(); // 설정한 내용을 바탕으로 SecurityFilterChain 빈을 생성해 스프링 시큐리티 필터 체인에 등록
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> { // 로그인 성공 시 전달되는 HTTP 요청, 응답 객체와 인증 정보를 인자로 받음
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal(); // OAuth2 사용자 정보를 DefaultOAuth2User로 캐스팅해 속성에 접근

            String id = defaultOAuth2User.getAttributes().get("id").toString(); // OAuth2 공급자로부터 받은 사용자 속성 중 id 값을 꺼냄
            String body = """
                    {"id":"%s"}
                    """.formatted(id); // JSON 응답 본문 작성

            // 응답 헤더 설정
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // 본문 전송
            PrintWriter writer = response.getWriter();
            writer.println(body);
            writer.flush();
        });
    }
}