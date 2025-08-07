package com.chocobi.leafy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient kakaoNaviWebClient(@Value("${kakao.navi.url}") String baseUrl, @Value("${kakao.api.key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    @Bean
    public WebClient tmapWebClient(@Value("${tmap.url}") String baseUrl, @Value("${kakao.api.key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("appKey", apiKey)
                .build();
    }
      
    @Bean
    public WebClient tourWebClient(@Value("${tour.api.base.url}") String tourBaseUrl) {
        return createWebClient(tourBaseUrl);
    }

    @Bean
    public WebClient cultureWebClient(@Value("${culture.api.base.url}") String cultureBaseUrl) {
        return createWebClient(cultureBaseUrl);
    }

    @Bean
    public WebClient farmWebClient(@Value("${farm.api.base.url}") String farmBaseUrl) {
        return createWebClient(farmBaseUrl);
    }

    @Bean
    public WebClient kakaoWebClient(@Value("${kakao.api.base.url}") String kakaoBaseUrl) {
        return createWebClient(kakaoBaseUrl);
    }

    private WebClient createWebClient(String baseUrl) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient.Builder builder = WebClient.builder()
                .uriBuilderFactory(factory)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(2 * 1024 * 1024)) // 2MB로 증가
                        .build())
                .baseUrl(baseUrl);

        return builder.build();
    }
}
