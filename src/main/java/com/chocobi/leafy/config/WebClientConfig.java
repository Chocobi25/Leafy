package com.chocobi.leafy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final KakaoConfig kakaoConfig;
    private final TmapConfig tmapConfig;

    public WebClientConfig(KakaoConfig kakaoConfig, TmapConfig tmapConfig) {
        this.kakaoConfig = kakaoConfig;
        this.tmapConfig = tmapConfig;
    }

    @Bean
    public WebClient kakaoNaviWebClient() {
        return WebClient.builder()
                .baseUrl("https://apis-navi.kakaomobility.com")
                .defaultHeader("Authorization", "KakaoAK " + kakaoConfig.getApiKey())
                .build();
    }

    @Bean
    public WebClient tmapWebClient() {
        return WebClient.builder()
                .baseUrl("https://apis.openapi.sk.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("appKey", tmapConfig.getApiKey())
                .build();
    }
}
