package com.github.giuseppemarletta.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF protection
            .authorizeHttpRequests(authorize -> authorize // Configure authorization rules
                .requestMatchers("/auth/**", "/hello", "/users/**").permitAll() // Allow all requests to /auth/** and /hello endpoints
                .anyRequest().authenticated() // Require authentication for any other requests
            )
            .httpBasic(httpBasic -> httpBasic.disable()); // Disable HTTP Basic authentication;
        return http.build();
    }

}
