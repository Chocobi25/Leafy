package com.chocobi.leafy.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("odsay")
@Getter
public class ODsayConfig {

    private final String apiKey;

    public ODsayConfig(String apiKey) {
        this.apiKey = apiKey;
    }
}
