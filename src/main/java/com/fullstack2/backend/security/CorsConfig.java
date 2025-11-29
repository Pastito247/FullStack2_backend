package com.fullstack2.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 🔹 Orígenes permitidos (ajusta el dominio del frontend en Render)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://fullstack2-frontend.onrender.com"
        ));

        // 🔹 Métodos permitidos (incluye OPTIONS para el preflight)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 🔹 Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // 🔹 Permitir cookies / Authorization header
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 👈 MUY IMPORTANTE: que cubra TODO /api/**
        source.registerCorsConfiguration("/api/**", config);

        return source;
    }
}
