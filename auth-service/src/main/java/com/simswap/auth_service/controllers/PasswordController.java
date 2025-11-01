package com.simswap.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.auth_service.dtos.ChangePasswordRequest;
import com.simswap.auth_service.dtos.RequestPasswordResetRequest;
import com.simswap.auth_service.dtos.ResetPasswordRequest;
import com.simswap.auth_service.services.PasswordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordController {

	private final PasswordService passwordService;
	
	/**
     * Endpoint pour changer le mot de passe d'un utilisateur connecté.
     *
     * @param request ChangePasswordRequest contenant :
     *                - authUserId : ID de l'utilisateur connecté
     *                - oldPassword : ancien mot de passe
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du nouveau mot de passe
     * @return ResponseEntity<Void> : HTTP 204 No Content si succès
     * 
     * @throws IllegalArgumentException si :
     *         - les nouveaux mots de passe ne correspondent pas
     *         - l'ancien mot de passe est incorrect
     *         - l'utilisateur n'existe pas
     */
	@PostMapping("/change-password")
	public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
		passwordService.changePassword(request);
		return ResponseEntity.noContent().build();
	}
	
	/**
     * Endpoint pour demander la réinitialisation du mot de passe.
     * Un email contenant un token sera envoyé à l'utilisateur.
     *
     * @param request RequestPasswordResetRequest contenant :
     *                - email : email de l'utilisateur
     * @return ResponseEntity<Void> : HTTP 204 No Content si succès
     * 
     * @throws IllegalArgumentException si l'utilisateur avec cet email n'existe pas
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody RequestPasswordResetRequest request) {
        passwordService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Endpoint pour réinitialiser le mot de passe via un token reçu par email.
     *
     * @param request ResetPasswordRequest contenant :
     *                - resetToken : token reçu par email
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du mot de passe
     * @return ResponseEntity<Void> : HTTP 204 No Content si succès
     * 
     * @throws IllegalArgumentException si les mots de passe ne correspondent pas ou si le token est invalide
     * @throws IllegalStateException si le token est expiré
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
