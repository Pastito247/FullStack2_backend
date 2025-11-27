package com.fullstack2.backend.config;

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

        // 🌐 ORÍGENES PERMITIDOS
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:4173",
                "https://forjayacero.netlify.app"  // <-- SIN la barra final
        ));

        // 🔁 Métodos permitidos
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 📦 Headers permitidos
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type"
        ));

        // Permitir cookies/tokens cruzados
        config.setAllowCredentials(true);

        // Aplicar a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
