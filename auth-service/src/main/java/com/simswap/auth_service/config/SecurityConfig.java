package com.simswap.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	private static final String[] WHITE_LIST_URL = {"/api/v1/auth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"};
	
    private final AuthenticationProvider authenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactive CSRF (utile pour API REST)
            .csrf(csrf -> csrf.disable())
            // Définir les règles d'accès
            .authorizeHttpRequests(req -> req
            		.requestMatchers(WHITE_LIST_URL).permitAll() 	// Routes auth permis
                    .anyRequest().authenticated() 					// Tout le reste nécessite une authentification
		    )
            // Pas de session côté serveur
		    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		    // Définit le provider d'authentification
		    .authenticationProvider(authenticationProvider);
            
//            // Pas de login form ou redirection
//            .formLogin(login -> login.disable())
//            .httpBasic(basic -> basic.disable())
//
//            // Autorise tout le monde sur toutes les routes
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()
//            );

        return http.build();
    }
}
