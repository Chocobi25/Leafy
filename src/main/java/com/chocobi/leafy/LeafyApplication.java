package com.chocobi.leafy;

import com.chocobi.leafy.config.KakaoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KakaoConfig.class)
public class LeafyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeafyApplication.class, args);
	}

}
