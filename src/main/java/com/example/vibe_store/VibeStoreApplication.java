package com.example.vibe_store;

import com.example.vibe_store.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class VibeStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(VibeStoreApplication.class, args);
	}
}
