//package com.simswap.gateway_service.config;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Configuration
//public class GatewayConfig {
//
//    /**
//     * Définir les routes personnalisées avec filtres et circuit breakers
//     */
//    @Bean
//    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
//        log.info("Configuration des routes du Gateway");
//        
//        return builder.routes()
//                
//                // ========== AUTH SERVICE ROUTES ==========
//                
//                // Route: Login/Authentication
//                .route("auth_authenticate", r -> r
//                        .path("/api/v1/auth/authenticate")
//                        .and().method(HttpMethod.POST)
//                        .filters(f -> f
//                                .circuitBreaker(c -> c
//                                        .setName("authAuthenticateCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/auth"))
//                                .filter((exchange, chain) -> {
//                                    log.info("→ Routing vers auth-service: POST /authenticate");
//                                    return chain.filter(exchange);
//                                })
//                        )
//                        .uri("lb://auth-service"))
//                
//                // Route: Registration
//                .route("auth_register", r -> r
//                        .path("/api/v1/auth/register")
//                        .and().method(HttpMethod.POST)
//                        .filters(f -> f
//                                .circuitBreaker(c -> c
//                                        .setName("authRegisterCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/auth"))
//                                .filter((exchange, chain) -> {
//                                    log.info("→ Routing vers auth-service: POST /register");
//                                    return chain.filter(exchange);
//                                })
//                        )
//                        .uri("lb://auth-service"))
//                
//                // Route: Refresh Token
//                .route("auth_refresh", r -> r
//                        .path("/api/v1/auth/refresh-token")
//                        .and().method(HttpMethod.POST)
//                        .filters(f -> f
//                                .circuitBreaker(c -> c
//                                        .setName("authRefreshCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/auth"))
//                                .filter((exchange, chain) -> {
//                                    log.info("→ Routing vers auth-service: POST /refresh-token");
//                                    return chain.filter(exchange);
//                                })
//                        )
//                        .uri("lb://auth-service"))
//                
//                // Route: Logout
//                .route("auth_logout", r -> r
//                        .path("/api/v1/auth/logout")
//                        .and().method(HttpMethod.POST)
//                        .filters(f -> f
//                                .circuitBreaker(c -> c
//                                        .setName("authLogoutCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/auth"))
//                                .filter((exchange, chain) -> {
//                                    log.info("→ Routing vers auth-service: POST /logout (authenticated)");
//                                    return chain.filter(exchange);
//                                })
//                        )
//                        .uri("lb://auth-service"))
//                
//                .route("auth_service", r -> r
//                	    .path("/api/v1/auth/**")
//                	    .filters(f -> f
//                	        .circuitBreaker(c -> c
//                	            .setName("authServiceCircuitBreaker")
//                	            .setFallbackUri("forward:/fallback/auth"))
//                	        .filter((exchange, chain) -> {
//                	            log.info("→ Routing vers auth-service: {} {}", 
//                	                     exchange.getRequest().getMethod(), 
//                	                     exchange.getRequest().getPath());
//                	            return chain.filter(exchange);
//                	        })
//                	    )
//                	    .uri("lb://auth-service"))
//                
//                // Route: Auth Service API Docs
//                .route("auth_docs", r -> r
//                        .path("/auth-service/v3/api-docs/**")
//                        .and().method(HttpMethod.GET)
//                        .filters(f -> f
//                                .filter((exchange, chain) -> {
//                                    log.debug("→ Routing vers auth-service API docs");
//                                    return chain.filter(exchange);
//                                })
//                        )
//                        .uri("lb://auth-service"))
//                
//                .build();
//    }
//}
