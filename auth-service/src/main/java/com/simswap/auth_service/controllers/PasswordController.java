package com.simswap.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.auth_service.dtos.ApiResponse;
import com.simswap.auth_service.dtos.ChangePasswordRequest;
import com.simswap.auth_service.dtos.PasswordResponse;
import com.simswap.auth_service.dtos.RequestPasswordResetRequest;
import com.simswap.auth_service.dtos.ResetPasswordRequest;
import com.simswap.auth_service.services.PasswordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Password Management", description = "Endpoints pour la gestion des mots de passe : changement, réinitialisation et demande de réinitialisation")
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
     * @return ResponseEntity<ApiResponse<Void>> : HTTP 200 si succès
     * 
     * @throws IllegalArgumentException si :
     *         - les nouveaux mots de passe ne correspondent pas
     *         - l'ancien mot de passe est incorrect
     *         - l'utilisateur n'existe pas
     */
	@PostMapping("/change-password")
	@Operation(
	    summary = "Changer le mot de passe d'un utilisateur connecté",
	    description = """
	        Permet à un utilisateur authentifié de modifier son mot de passe en vérifiant :
	            - La correspondance entre le nouveau mot de passe et sa confirmation
	            - La validité de l'ancien mot de passe
	            - L'existence de l'utilisateur associé à l'authUserId

	        Fonctionnement :
	            - Vérification que les mots de passe (newPassword / confirmNewPassword) correspondent
	            - Récupération de l’utilisateur via son authUserId
	            - Validation de l'ancien mot de passe
	            - Encodage et mise à jour du nouveau mot de passe dans la base

	        Remarques importantes :
	            - Une erreur est renvoyée si l’ancien mot de passe est incorrect
	            - Une erreur est renvoyée si l'utilisateur n'existe pas
	            - Nécessite que l'utilisateur soit déjà authentifié (authUserId fourni par le frontend)
	    """
	)
	public ResponseEntity<ApiResponse<Void>> changePassword(@RequestHeader("X-User-Email") String email, @RequestBody ChangePasswordRequest request) {
	    ApiResponse<Void> response = passwordService.changePassword(email, request);
	    return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	/**
     * Endpoint pour demander la réinitialisation du mot de passe.
     * Un email contenant un token sera envoyé à l'utilisateur.
     *
     * @param request RequestPasswordResetRequest contenant :
     *                - email : email de l'utilisateur
     * @return ResponseEntity<ApiResponse<Void>> : HTTP 202 Accepted si succès
     * 
     * @throws IllegalArgumentException si l'utilisateur avec cet email n'existe pas
     */
    @PostMapping("/request-password-reset")
    @Operation(
	    summary = "Demander une réinitialisation de mot de passe",
	    description = """
	        Permet à un utilisateur de demander la réinitialisation de son mot de passe.
	        Un email contenant un lien avec un token de réinitialisation sera envoyé.

	        Fonctionnement :
	            - Vérification que l'email appartient à un utilisateur existant
	            - Génération d'un token unique, valable 1 heure
	            - Création et sauvegarde du PasswordResetToken (non utilisé)
	            - Génération du lien de réinitialisation destiné à l'email
	            - Envoi du lien via EmailService/Kafka (à venir)

	        Remarques importantes :
	            - Une erreur est renvoyée si aucun utilisateur ne correspond à l'email
	            - Aucun JWT n'est requis pour cette opération
	    """
	)
    public ResponseEntity<ApiResponse<PasswordResponse>> requestPasswordReset(@RequestBody RequestPasswordResetRequest request) {
        ApiResponse<PasswordResponse> response = passwordService.requestPasswordReset(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * Endpoint pour réinitialiser le mot de passe via un token reçu par email.
     *
     * @param request ResetPasswordRequest contenant :
     *                - resetToken : token reçu par email
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du mot de passe
     * @return ResponseEntity<ApiResponse<Void>> : HTTP 200 si succès
     * 
     * @throws IllegalArgumentException si les mots de passe ne correspondent pas ou si le token est invalide
     * @throws IllegalStateException si le token est expiré
     */
    @PostMapping("/reset-password")
    @Operation(
	    summary = "Réinitialiser le mot de passe via un token reçu par email",
	    description = """
	        Permet de définir un nouveau mot de passe à partir d'un token envoyé par email.

	        Fonctionnement :
	            - Recherche du token de réinitialisation (valide et non utilisé)
	            - Vérification que le token n'est pas expiré
	            - Vérification de la correspondance entre les mots de passe fournis
	            - Encodage et mise à jour du nouveau mot de passe
	            - Marquage du token comme utilisé pour empêcher toute réutilisation

	        Remarques importantes :
	            - Une erreur est renvoyée si le token est expiré ou invalide
	            - Une erreur est renvoyée si les mots de passe ne correspondent pas
	            - Aucune authentification JWT n’est nécessaire pour cette opération
	    """
	)
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        ApiResponse<Void> response = passwordService.resetPassword(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
