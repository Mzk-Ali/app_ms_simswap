package com.simswap.gateway_service.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.gateway_service.dtos.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {
	@GetMapping
    public ResponseEntity<ErrorResponse> genericFallback() {
        log.error("Fallback générique du circuit breaker déclenché");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .code("SERVICE_UNAVAILABLE")
                .message("Le service demandé est temporairement indisponible. Veuillez réessayer dans quelques instants.")
                .traceId(null)
                .service("gateway-service")
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
	
	@RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<ErrorResponse> authServiceFallback() {
        log.error("Fallback du circuit breaker du service auth déclenché");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .code("SERVICE_UNAVAILABLE")
                .message("Le service d'authentification est temporairement indisponible.  Veuillez réessayer dans quelques instants.")
                .traceId(null)
                .service("auth-service")
                .path("/fallback/auth")
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
	
	/**
     * Fallback dynamique basé sur le nom du service
     */
//    @GetMapping("/{service}")
//    public ResponseEntity<Map<String, Object>> serviceFallback(@PathVariable String service) {
//        log.error("Fallback du circuit breaker déclenché pour le service: {}", service);
//        
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//                .body(Map.of(
//                        "error", "Service indisponible",
//                        "message", String.format("Le service %s est temporairement indisponible. Veuillez réessayer plus tard.", service),
//                        "service", service,
//                        "timestamp", LocalDateTime.now(),
//                        "status", HttpStatus.SERVICE_UNAVAILABLE.value()
//                ));
//    }
}
