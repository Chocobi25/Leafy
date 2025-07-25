package com.chocobi.leafy.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kakao")
@Getter
public class KakaoConfig {

    private final String apiKey;

    public KakaoConfig(String apiKey) {
        this.apiKey = apiKey;
    }
}
