package com.chocobi.leafy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;


@Configuration
public class WebClientConfig {

    // 생태관광 API
    @Bean
    public WebClient tourWebClient(@Value("${tour.api.base.url}") String tourApiBaseUrl) {
        return createWebClient(tourApiBaseUrl);
    }

    // 테마관광 API, 농어촌체험 API
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

    @Bean
    public WebClient naverWebClient(@Value("${naver.base.url}") String naverBaseUrl,
                                    @Value("${naver.client.id}") String clientId,
                                    @Value("${naver.client.secret}") String clientSecret) {

        return WebClient.builder()
                .baseUrl(naverBaseUrl)
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .uriBuilderFactory(createUriBuilderFactory(naverBaseUrl))
                .build();
    }


    private WebClient createWebClient(String baseUrl) {
        return WebClient.builder()
                .uriBuilderFactory(createUriBuilderFactory(baseUrl))
                .exchangeStrategies(commonExchangeStrategies())
                .baseUrl(baseUrl)
                .build();
    }

    private DefaultUriBuilderFactory createUriBuilderFactory(String baseUrl) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return factory;
    }

    private ExchangeStrategies commonExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs()
                            .maxInMemorySize(2 * 1024 * 1024);
                    })
                .build();
    }
}
