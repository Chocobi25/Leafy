package com.chocobi.leafy.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tmap")
@Getter
public class TmapConfig {

    private final String apiKey;

    public TmapConfig(String apiKey) {
        this.apiKey = apiKey;
    }
}