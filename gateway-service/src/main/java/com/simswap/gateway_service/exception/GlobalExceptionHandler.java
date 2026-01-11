package com.simswap.gateway_service.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import com.simswap.gateway_service.dtos.ErrorResponse;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	private final Tracer tracer;
	
	public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }
	
	/**
     * Gérer les échecs d'authentification (identifiants invalides)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("Échec de l'authentification: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
        		.timestamp(LocalDateTime.now().toString())
        		.status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .code("INVALID_CREDENTIALS")
                .message("Identifiants invalides")
                .traceId(generateTraceId())
                .service("gateway-service")
        		.build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Gérer les échecs d'autorisation (permissions insuffisantes)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.error("Accès refusé: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .code("FORBIDDEN")
                .message("Vous n'avez pas les permissions nécessaires")
                .traceId(generateTraceId())
                .service("gateway-service")
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Gérer les exceptions de statut de réponse
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.error("Exception de statut de réponse: {} - {}", ex.getStatusCode(), ex.getReason());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(ex.getStatusCode().value())
                .error(ex.getStatusCode().toString())
                .code("INTERNAL_SERVER_ERROR")
                .message(ex.getReason() != null ? ex.getReason() : "Une erreur s'est produite")
                .traceId(generateTraceId())
                .service("gateway-service")
                .build();
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
    
    /**
     * Gérer toutes les autres exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erreur inattendue: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .code("INTERNAL_SERVER_ERROR")
                .message("Une erreur inattendue s'est produite")
                .traceId(generateTraceId())
                .service("gateway-service")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private String generateTraceId() {
        return tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : null;
    }
}

/**
 * Attributs d'erreur personnalisés
 * 
 * Fournit des attributs d'erreur personnalisés pour la gestion des erreurs WebFlux
 */
@Component
class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        
        // Personnaliser la réponse d'erreur
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        errorAttributes.remove("trace"); // Supprimer la stack trace de la réponse
        
        return errorAttributes;
    }
}
