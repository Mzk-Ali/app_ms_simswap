package com.simswap.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactive CSRF (utile pour API REST)
            .csrf(csrf -> csrf.disable())
            
            // Pas de login form ou redirection
            .formLogin(login -> login.disable())
            .httpBasic(basic -> basic.disable())

            // Autorise tout le monde sur toutes les routes
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
