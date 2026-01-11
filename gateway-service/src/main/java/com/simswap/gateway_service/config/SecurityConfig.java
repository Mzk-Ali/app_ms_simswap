package com.simswap.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import com.simswap.gateway_service.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebFluxSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		log.info("Configuration de la chaîne de filtres de sécurité");
		return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(
                    		"/api/v1/auth/authenticate",
                    		"/api/v1/auth/register",
                    		"/swagger-ui.html",
                    		"/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/auth-service/v3/api-docs",
                            "/actuator/**" 
                    	).permitAll()
                        .pathMatchers("/api/v1/auth/logout").authenticated()
                        .anyExchange().authenticated()
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            log.error("Erreur d'authentification: {}", ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, denied) -> {
                            log.error("Accès refusé: {}", denied.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .build();
    }


//    @Bean
//    public AuthenticationWebFilter jwtAuthenticationWebFilter(
//    		JwtReactiveAuthenticationManager authManager,
//    		JwtServerAuthenticationConverter converter
//    ) {
//        AuthenticationWebFilter filter = new AuthenticationWebFilter(authManager);
//        filter.setServerAuthenticationConverter(converter);
//        filter.setRequiresAuthenticationMatcher(
//            new NegatedServerWebExchangeMatcher(
//                ServerWebExchangeMatchers.pathMatchers(
//                    "/api/v1/auth/authenticate",
//                    "/api/v1/auth/authenticate/**",
//                    "/api/v1/auth/register",
//                    "/swagger-ui.html",
//                    "/swagger-ui/**",
//                    "/v3/api-docs/**",
//                    "/auth-service/v3/api-docs",
//                    "/actuator/**"
//                )
//            )
//        );
//        return filter;
//    }
}
