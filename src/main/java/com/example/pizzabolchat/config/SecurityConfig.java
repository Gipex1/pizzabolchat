package com.example.pizzabolchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Включаем CORS (используем настройки из твоего CorsConfig)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Отключаем CSRF (защиту от подделки запросов, для REST API она не нужна)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Настраиваем правила доступа
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем всем желающим регистрироваться и логиниться
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        // Всё остальное (если надо) закрываем
                        .anyRequest().permitAll() // Пока разрешим всё, чтобы не мешать тестам
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}