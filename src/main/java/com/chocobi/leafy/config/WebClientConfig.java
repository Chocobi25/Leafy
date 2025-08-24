package com.chocobi.leafy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Configuration
public class WebClientConfig {

    // 생태관광 API, 관광사진 API
    @Bean
    public WebClient tourWebClient(@Value("${tour.api.base.url}") String tourApiBaseUrl,
                                   @Value("${tour.api.key}") String tourApiKey) {
        return createWebClient(tourApiBaseUrl, tourApiKey);
    }

    @Bean
    public WebClient ruralWebClient(@Value("${culture.api.base.url}") String cultureBaseUrl,
                                    @Value("${rural.api.key}") String ruralApiKey) {
        return createWebClient(cultureBaseUrl, ruralApiKey);
    }

    @Bean
    public WebClient themeWebClient(@Value("${culture.api.base.url}") String cultureBaseUrl,
                                    @Value("${theme.api.key}") String themeApiKey) {
        return createWebClient(cultureBaseUrl, themeApiKey);
    }

    @Bean
    public WebClient farmWebClient(@Value("${farm.api.base.url}") String farmBaseUrl,
                                   @Value("${farm.api.key}") String farmApiKey) {
        return createWebClient(farmBaseUrl, farmApiKey);
    }

    @Bean
    public WebClient kakaoWebClient(@Value("${kakao.api.base.url}") String kakaoBaseUrl,
                                    @Value("${kakao.api.key}") String kakaoApiKey) {
        return WebClient.builder()
                .baseUrl(kakaoBaseUrl)
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                .build();
    }

    private WebClient createWebClient(String baseUrl, String serviceKey) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .exchangeStrategies(commonExchangeStrategies())
                .baseUrl(baseUrl)
                .defaultUriVariables(Map.of("ServiceKey", serviceKey))
                .build();
    }

    private ExchangeStrategies commonExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
