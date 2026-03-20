package com.notesapp.config;

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

        // Orígenes permitidos (Flutter web, emulador Android, admin panel, dispositivo físico)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // Admin panel React (production)
                "http://localhost:5173",   // Admin panel Vite dev server
                "http://localhost:8080",   // Backend / Flutter web
                "http://10.0.2.2:8080"    // Emulador Android accede al host con esta IP
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Permitir envío de cookies/tokens
        config.setAllowCredentials(true);

        // Tiempo que el navegador cachea la respuesta preflight (1 hora)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
