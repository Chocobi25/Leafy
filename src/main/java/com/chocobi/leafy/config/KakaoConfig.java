package com.chocobi.leafy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kakao")
@Getter @Setter
public class KakaoConfig {
    private String apiKey;
}
