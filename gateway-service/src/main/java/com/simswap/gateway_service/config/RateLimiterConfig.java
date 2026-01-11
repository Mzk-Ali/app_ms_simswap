package com.simswap.gateway_service.config;

import java.util.Optional;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class RateLimiterConfig {

    @PostConstruct
    public void init() {
        log.info("RateLimiterConfig chargé !");
    }

	@Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(addr -> addr.getAddress().getHostAddress())
                    .orElse("unknown-ip");
            
            log.debug("Clé Rate Limiter (IP): {}", ip);
            return Mono.just(ip);
        };
    }
	
	@Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Essayer de récupérer l'email de l'utilisateur du header (défini par le filtre JWT)
            String userEmail = Optional.ofNullable(
                    exchange.getRequest().getHeaders().getFirst("X-User-Email")
            ).orElse(null);
            
            // Fallback sur l'IP si pas d'email
            if (userEmail == null) {
                String ip = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(addr -> addr.getAddress().getHostAddress())
                        .orElse("unknown-ip");
                
                log.debug("Clé Rate Limiter (IP fallback): {}", ip);
                return Mono.just(ip);
            }
            
            log.debug("Clé Rate Limiter (User): {}", userEmail);
            return Mono.just(userEmail.toLowerCase());
        };
    }
	
	@Bean
    public KeyResolver ipAndUserKeyResolver() {
        return exchange -> {
            String ip = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(addr -> addr.getAddress().getHostAddress())
                    .orElse("unknown-ip");

            String userEmail = Optional.ofNullable(
                    exchange.getRequest().getHeaders().getFirst("X-User-Email")
            ).orElse("anonymous");

            String combinedKey = ip + ":" + userEmail.toLowerCase();
            
            log.debug("Clé Rate Limiter (Combinée): {}", combinedKey);
            return Mono.just(combinedKey);
        };
    }
}
