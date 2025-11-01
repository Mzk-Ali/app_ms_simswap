package com.simswap.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.auth_service.dtos.AuthenticationRequest;
import com.simswap.auth_service.dtos.RefreshTokenRequest;
import com.simswap.auth_service.dtos.RegisterRequest;
import com.simswap.auth_service.dtos.TokensResponse;
import com.simswap.auth_service.dtos.VerifyEmailRequest;
import com.simswap.auth_service.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	
	@GetMapping("")
	public String index() {
		return "API Auth Service fonctionne";
	}
	
	/**
     * Endpoint pour l'inscription d'un nouvel utilisateur.
     *
     * @param request RegisterRequest contenant :
     *                - email : email de l'utilisateur
     *                - password : mot de passe
     * @return ResponseEntity<Void> : HTTP 200 OK si succès
     * 
     * @throws IllegalArgumentException si l'utilisateur existe déjà
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint pour l'authentification d'un utilisateur.
     *
     * @param request AuthenticationRequest contenant :
     *                - email : email de l'utilisateur
     *                - password : mot de passe
     *                - deviceName : nom du device
     *                - userAgent : user-agent du navigateur ou client
     *                - ipAddress : adresse IP de l'utilisateur
     * @return ResponseEntity<TokensResponse> : JWT et refresh token
     * 
     * @throws IllegalArgumentException si l'email ou le mot de passe sont incorrects
     * @throws IllegalStateException si l'utilisateur est banni ou non vérifié
     */
    @PostMapping("/authenticate")
    public ResponseEntity<TokensResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    /**
     * Endpoint pour rafraîchir le token d'authentification.
     *
     * @param request RefreshTokenRequest contenant :
     *                - refreshToken : token de rafraîchissement
     *                - deviceName : nom du device
     *                - userAgent : user-agent
     *                - ipAddress : adresse IP
     * @return ResponseEntity<TokensResponse> : nouveaux JWT et refresh token
     * 
     * @throws IllegalArgumentException si le refresh token est invalide
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokensResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
    
    /**
     * Endpoint pour se déconnecter d'un device spécifique.
     *
     * @param request RefreshTokenRequest contenant :
     *                - refreshToken : token de rafraîchissement
     *                - deviceName : nom du device
     *                - userAgent : user-agent
     *                - ipAddress : adresse IP
     * @return ResponseEntity<Void> : HTTP 200 OK si succès
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
    	authService.logout(request);
    	return ResponseEntity.ok().build();
    }
    
    /**
     * Endpoint pour vérifier l'email d'un utilisateur via un token.
     *
     * @param request VerifyEmailRequest contenant :
     *                - verifyEmailToken : token reçu par email
     * @return ResponseEntity<Void> : HTTP 200 OK si l'email est vérifié
     * 
     * @throws IllegalArgumentException si le token est invalide ou déjà utilisé
     * @throws IllegalStateException si l'utilisateur est banni ou déjà vérifié
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }
}
