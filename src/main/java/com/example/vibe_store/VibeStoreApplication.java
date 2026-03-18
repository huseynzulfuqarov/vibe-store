package com.example.vibe_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class VibeStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(VibeStoreApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(
								"http://localhost:5173",
								"http://localhost:8080",
								"http://logistore.tech",
								"https://logistore.tech",
								"http://www.logistore.tech",
								"https://www.logistore.tech",
								"http://104.248.30.129"
						)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}
}
