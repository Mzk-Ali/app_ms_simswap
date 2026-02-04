package com.simswap.gateway_service.services;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {
	@Value("${application.security.jwt.secret-key}")
	private String secretKey;
	
	public String extractUsername(String token) {
	    return extractClaim(token, Claims::getSubject);
	}
	
	public String extractAuthUserId(String token) {
	    return extractClaim(token, claims -> claims.get("authUserId", String.class));
	}
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
	    final Claims claims = extractAllClaims(token);
	    return claimsResolver.apply(claims);
	}
	
	public Boolean isTokenValid(String token) {
        try {
            // Parser et valider le token
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            
            // Vérifier l'expiration
            if (isTokenExpired(token)) {
                log.warn("Le token JWT a expiré");
                return false;
            }
            
            // Valider la présence du username
            String username = extractUsername(token);
            if (username == null || username.isEmpty()) {
                log.warn("Le token JWT n'a pas de username/subject");
                return false;
            }
            
            log.debug("Token JWT valide pour l'utilisateur: {}", username);
            return true;
            
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expiré: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT non supporté: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Token JWT mal formé: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Échec de la validation de la signature JWT: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Token JWT invalide: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token: {}", e.getMessage());
            return false;
        }
    }
	
	@SuppressWarnings("unchecked")
	public List<String> extractRoles(String token) {
	    Claims claims = extractAllClaims(token);
	    Object rolesObj = claims.get("role"); 
	    
	    if (rolesObj instanceof String) {
	        return List.of((String) rolesObj);
	    } else if (rolesObj instanceof List) {
	        return (List<String>) rolesObj;
	    }
	    
	    return List.of();
	}
	
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts
	        .parserBuilder()
	        .setSigningKey(getSignInKey())
	        .build()
	        .parseClaimsJws(token)
	        .getBody();
	}
	
	private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
