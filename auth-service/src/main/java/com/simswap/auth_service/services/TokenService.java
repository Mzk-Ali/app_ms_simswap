package com.simswap.auth_service.services;

import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class TokenService {
	public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hash du token", e);
        }
    }

    public boolean matches(String rawToken, String hashedToken) {
        return hashToken(rawToken).equals(hashedToken);
    }
}

