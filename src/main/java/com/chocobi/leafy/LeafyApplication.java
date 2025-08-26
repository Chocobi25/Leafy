package com.chocobi.leafy;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeafyApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		String dbPassword = dotenv.get("DB_PASSWORD");
		System.setProperty("DB_PASSWORD",
				dbPassword != null ? dbPassword : "기본비밀번호나 예외처리");

		SpringApplication.run(LeafyApplication.class, args);
	}
}
