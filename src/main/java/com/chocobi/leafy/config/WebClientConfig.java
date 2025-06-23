package com.chocobi.leafy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {
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
