package com.simswap.auth_service.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.simswap.auth_service.dtos.ApiResponse;
import com.simswap.auth_service.dtos.ChangePasswordRequest;
import com.simswap.auth_service.dtos.PasswordResponse;
import com.simswap.auth_service.dtos.RequestPasswordResetRequest;
import com.simswap.auth_service.dtos.ResetPasswordRequest;
import com.simswap.auth_service.entities.PasswordResetToken;
import com.simswap.auth_service.entities.User;
import com.simswap.auth_service.publishers.EmailPublisher;
import com.simswap.auth_service.repositories.PasswordResetTokenRepository;
import com.simswap.auth_service.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
	
	private final UserRepository userRepository;
	private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailPublisher emailPublisher;
    
    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordBaseUrl;
	
    /**
     * Change le mot de passe d'un utilisateur existant.
     *
     * @param request ChangePasswordRequest contenant :
     *                - authUserId : ID de l'utilisateur
     *                - oldPassword : ancien mot de passe
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du nouveau mot de passe
     * @return ApiResponse<Void>
     *
     * Étapes :
     * 1. Vérifie que le nouveau mot de passe et la confirmation correspondent.
     * 2. Récupère l'utilisateur à partir de son authUserId.
     * 3. Vérifie que l'ancien mot de passe fourni est correct.
     * 4. Encode et met à jour le mot de passe dans la base de données.
     */
	public ApiResponse<Void> changePassword(String email, ChangePasswordRequest request) {
		log.info("Début de la demande de changement de mot de passe pour l'utilisateur ID : {}", request.getAuthUserId());
		
		// Vérifie les nouveaux mots de passe
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
        	log.warn("Les nouveaux mots de passe ne correspondent pas pour l'utilisateur ID : {}", request.getAuthUserId());
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
        
        // Récupère l'utilisateur
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Aucun utilisateur trouvé pour l'utilisateur : {}", email);
					return new IllegalArgumentException("Utilisateur introuvable");
				});
		log.debug("Utilisateur trouvé : {}", user.getEmail());
		
		// Vérifie l'ancien mot de passe
		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Ancien mot de passe incorrect pour {} !", user.getEmail());
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }
		log.debug("Ancien mot de passe vérifié avec succès pour l'utilisateur {}", user.getEmail());
		
		// Met à jour le mot de passe
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe mis à jour avec succès pour {} !", user.getEmail());
        
        return ApiResponse.<Void>builder()
                .status(200)
                .success(true)
                .message("Votre mot de passe a été modifié avec succès.")
                .build();
	}
	
	/**
     * Génère un token de réinitialisation de mot de passe pour un utilisateur donné.
     *
     * @param request RequestPasswordResetRequest contenant :
     *                - email : email de l'utilisateur
     * @return ApiResponse<PasswordResponse>
     *
     * Étapes :
     * 1. Récupère l'utilisateur à partir de son email.
     * 2. Génère un token unique.
     * 3. Crée un objet PasswordResetToken avec une date d'expiration et un statut "non utilisé".
     * 4. Sauvegarde le token dans la base de données.
     * 5. Génère le lien de réinitialisation et le prépare pour l'envoi par email.
     */
	public ApiResponse<PasswordResponse> requestPasswordReset(RequestPasswordResetRequest request) {
		log.info("Demande d'un token de reset password pour {} ...", request.getEmail());
		
		User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                	log.warn("Aucun utilisateur trouvé pour {}", request.getEmail());
                	return new IllegalArgumentException("Utilisateur introuvable");
                });
		
		// Générer un token unique
        String token = UUID.randomUUID().toString();
        log.debug("Token généré : {} pour l'utilisateur {}", token, user.getEmail());
        
        // Création d'un PasswordResetToken
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(60, ChronoUnit.MINUTES)) // 1 heure de validité
                .used(false)
                .build();
		
        // Sauvegarde du PasswordResetToken dans la base de donnée
        tokenRepository.save(resetToken);
        log.info("Token de réinitialisation sauvegardé pour l'utilisateur {}", user.getEmail());
        
        // Envoyer un email avec le lien
        String resetLink = "https://ton-frontend.com/reset-password?token=" + token;
        log.debug("Lien de réinitialisation généré : {}", resetLink);
        
        String resetPasswordUrl = resetPasswordBaseUrl + "/" + token;
	    log.info("URL de réinitialisation de mot de passe : {}", resetPasswordUrl);
	    emailPublisher.sendForgotPasswordEmail(user.getEmail(), token, resetPasswordUrl);
        
        log.info("Lien de réinitialisation envoyé à {}", user.getEmail());
        
        PasswordResponse response = PasswordResponse.builder()
                .email(user.getEmail())
                .message("Un lien de réinitialisation est envoyé à l'email : " + user.getEmail())
                .build();
        
        return ApiResponse.<PasswordResponse>builder()
                .status(202)
                .success(true)
                .message("Un lien de réinitialisation est envoyé à l'email : " + user.getEmail())
                .data(response)
                .build();
	}
	
	/**
     * Réinitialise le mot de passe d'un utilisateur à partir d'un token reçu par email.
     *
     * @param request ResetPasswordRequest contenant :
     *                - resetToken : token reçu par email
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du nouveau mot de passe
     * @return ApiResponse<Void>
     *
     * Étapes :
     * 1. Récupère le token de réinitialisation valide (non utilisé et existant).
     * 2. Vérifie que le token n'est pas expiré.
     * 3. Vérifie que le nouveau mot de passe et la confirmation correspondent.
     * 4. Encode et met à jour le mot de passe dans la base de données.
     * 5. Marque le token comme utilisé pour éviter toute réutilisation.
     */
	public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
		log.info("Demande de réinitialisation de mot de passe POST-EMAIL avec le token : {} ...", request.getResetToken());
		
		PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(request.getResetToken())
                .orElseThrow(() -> {
                	log.warn("Token de réinitialisation invalide ou déjà utilisé : {}", request.getResetToken());
                	return new IllegalArgumentException("Token invalide ou expiré");
                });
		
		if (resetToken.isExpired()) {
			log.warn("Token expiré pour l'utilisateur {} (token : {})", resetToken.getUser().getEmail(), resetToken.getToken());
            throw new IllegalStateException("Token expiré");
        }
		
		// Vérifie la concordance des mots de passe
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
        	log.warn("Échec de réinitialisation : les mots de passe ne correspondent pas pour l'utilisateur {}", resetToken.getUser().getEmail());
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
		
        // Mise à jour du mot de passe
		User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Mot de passe réinitialisé est sauvegardé avec succès pour l'utilisateur {}", user.getEmail());
        
    	// Mise à jour du token comme utilisé
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        log.info("Mot de passe réinitialisé pour l'utilisateur {}", user.getEmail());
        
        return ApiResponse.<Void>builder()
                .status(200)
                .success(true)
                .message("Votre mot de passe a été réinitialisé avec succès.")
                .build();
	}
}
