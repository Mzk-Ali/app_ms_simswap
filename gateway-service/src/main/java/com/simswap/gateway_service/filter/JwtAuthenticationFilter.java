package com.simswap.gateway_service.filter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.simswap.gateway_service.services.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
	private final JwtService jwtService;
	
	private static final String BEARER_PREFIX = "Bearer ";
	private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/authenticate",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/webjars"
    );
	
	@Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        HttpMethod method = request.getMethod();
        
        if (HttpMethod.OPTIONS.equals(method)) {
            log.debug("OPTIONS request detected, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }
        
        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            log.debug("Chemin public détecté, skip de l'authentification JWT: {}", path);
            return chain.filter(exchange);
        }
        
        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // No token provided
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Pas de token JWT trouvé pour: {}", path);
            return chain.filter(exchange);
        }
        
        try {
            // Extract token
            String token = authHeader.substring(BEARER_PREFIX.length());
            log.debug("Token JWT extrait pour: {}", path);
            
            // Validate token
            if (!jwtService.isTokenValid(token)) {
                log.warn("Token JWT invalide ou expiré pour: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // Extract user information
            String username = jwtService.extractUsername(token);
            List<String> roles = jwtService.extractRoles(token);
            
            log.info("Authentification réussie - User: {}, Roles: {}, Path: {}", username, roles, path);
            
            // Convert roles to Spring Security authorities
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            
            // Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            // Continue with authenticated context
//            return chain.filter(exchange)
//                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            
            SecurityContext securityContext = new SecurityContextImpl(authentication);
            
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> builder.header("X-User-Email", username))
                    .build();

            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement du token JWT: {}", e.getMessage(), e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
    
    /**
     * Vérifie si le chemin est public (ne nécessite pas d'authentification)
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
