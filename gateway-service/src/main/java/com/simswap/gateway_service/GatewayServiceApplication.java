package com.simswap.gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {
	
	public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
	
    /**
     * Fallback générique pour les circuits breakers.
     */
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("Service temporairement indisponible. Merci de réessayer plus tard.");
    }
    
    /**
     * Rate limiter Redis configuration
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // 5 requêtes par seconde, burst max 10
        return new RedisRateLimiter(5, 10);
    }

    /**
     * Définition des routes dynamiques + custom statiques
     */
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Exemple d'une route statique vers un service externe
                .route("docs_route", r -> r.path("/docs/**")
                        .filters(f -> f
                                .rewritePath("/docs/(?<path>.*)", "/${path}")
                                .circuitBreaker(c -> c
                                        .setName("docsCircuitBreaker")
                                        .setFallbackUri("forward:/fallback"))
                        )
                        .uri("https://spring.io")) // exemple externe
                // Exemple de route interne via service Eureka
                .route("auth_service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/auth/(?<segment>.*)", "/${segment}")
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()))
                                .circuitBreaker(c -> c.setName("authCB").setFallbackUri("forward:/fallback"))
                        )
                        .uri("lb://auth-service"))
                .build();
    }

    /**
     * Route dynamique basée sur Eureka
     * (complète automatiquement toutes les routes des services enregistrés)
     */
    @Bean
    public DiscoveryClientRouteDefinitionLocator discoveryRoutes(
            org.springframework.cloud.client.discovery.ReactiveDiscoveryClient discoveryClient,
            DiscoveryLocatorProperties properties
    ) {
        properties.setLowerCaseServiceId(true);
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, properties);
    }
    

    /**
     * KeyResolver (IP-based) pour le rate limiter
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }
}
