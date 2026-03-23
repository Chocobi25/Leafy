package com.chocobi.leafy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LeafyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeafyApplication.class, args);
	}
}
