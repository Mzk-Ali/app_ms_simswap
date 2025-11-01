package com.simswap.auth_service.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.simswap.auth_service.dtos.ChangePasswordRequest;
import com.simswap.auth_service.dtos.RequestPasswordResetRequest;
import com.simswap.auth_service.dtos.ResetPasswordRequest;
import com.simswap.auth_service.entities.PasswordResetToken;
import com.simswap.auth_service.entities.User;
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
	
    /**
     * Change le mot de passe d'un utilisateur existant.
     *
     * @param request ChangePasswordRequest contenant :
     *                - authUserId : ID de l'utilisateur
     *                - oldPassword : ancien mot de passe
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du nouveau mot de passe
     * @return void
     *
     * Étapes :
     * 1. Vérifie que le nouveau mot de passe et la confirmation correspondent.
     * 2. Récupère l'utilisateur à partir de son authUserId.
     * 3. Vérifie que l'ancien mot de passe fourni est correct.
     * 4. Encode et met à jour le mot de passe dans la base de données.
     */
	public void changePassword(ChangePasswordRequest request) {
		log.info("Début de la demande de changement de mot de passe pour l'utilisateur ID : {}", request.getAuthUserId());
		
		// Vérifie les nouveaux mots de passe
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
        	log.warn("Les nouveaux mots de passe ne correspondent pas pour l'utilisateur ID : {}", request.getAuthUserId());
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
        
        // Récupère l'utilisateur
		User user = userRepository.findByAuthUserId(request.getAuthUserId())
				.orElseThrow(() -> {
					log.warn("Aucun utilisateur trouvé pour l'utilisateur ID : {}", request.getAuthUserId());
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
	}
	
	/**
     * Génère un token de réinitialisation de mot de passe pour un utilisateur donné.
     *
     * @param request RequestPasswordResetRequest contenant :
     *                - email : email de l'utilisateur
     * @return void
     *
     * Étapes :
     * 1. Récupère l'utilisateur à partir de son email.
     * 2. Génère un token unique.
     * 3. Crée un objet PasswordResetToken avec une date d'expiration et un statut "non utilisé".
     * 4. Sauvegarde le token dans la base de données.
     * 5. Génère le lien de réinitialisation et le prépare pour l'envoi par email.
     */
	public void requestPasswordReset(RequestPasswordResetRequest request) {
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
        
        // Envoi vers EmailService via Kafka
        
        log.info("Lien de réinitialisation envoyé à {}", user.getEmail());
	}
	
	/**
     * Réinitialise le mot de passe d'un utilisateur à partir d'un token reçu par email.
     *
     * @param request ResetPasswordRequest contenant :
     *                - resetToken : token reçu par email
     *                - newPassword : nouveau mot de passe
     *                - confirmNewPassword : confirmation du nouveau mot de passe
     * @return void
     *
     * Étapes :
     * 1. Récupère le token de réinitialisation valide (non utilisé et existant).
     * 2. Vérifie que le token n'est pas expiré.
     * 3. Vérifie que le nouveau mot de passe et la confirmation correspondent.
     * 4. Encode et met à jour le mot de passe dans la base de données.
     * 5. Marque le token comme utilisé pour éviter toute réutilisation.
     */
	public void resetPassword(ResetPasswordRequest request) {
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
        
    	// Mise à jour le token comme utilisé
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        log.info("Mot de passe réinitialisé pour l'utilisateur {}", user.getEmail());
	}
}
