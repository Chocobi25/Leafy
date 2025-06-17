package com.chocobi.leafy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.stereotype.Component;

@ConfigurationProperties("kakao")
@Getter
public class KakaoConfig {

    private final String apiKey;

    public KakaoConfig(String apiKey) {
        this.apiKey = apiKey;
    }
}
