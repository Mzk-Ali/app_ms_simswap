package com.simswap.auth_service.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.simswap.auth_service.dtos.ApiResponse;
import com.simswap.auth_service.dtos.AuthenticationRequest;
import com.simswap.auth_service.dtos.RefreshTokenRequest;
import com.simswap.auth_service.dtos.RegisterRequest;
import com.simswap.auth_service.dtos.RegisterResponse;
import com.simswap.auth_service.dtos.TokensResponse;
import com.simswap.auth_service.dtos.UserResponse;
import com.simswap.auth_service.dtos.VerifyEmailRequest;
import com.simswap.auth_service.entities.EmailVerificationToken;
import com.simswap.auth_service.entities.Role;
import com.simswap.auth_service.entities.Session;
import com.simswap.auth_service.entities.User;
import com.simswap.auth_service.publishers.EmailPublisher;
import com.simswap.auth_service.repositories.UserRepository;
import com.simswap.auth_service.repositories.EmailVerificationTokenRepository;
import com.simswap.auth_service.repositories.SessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	private final UserRepository userRepository;
	private final SessionRepository sessionRepository;
	private final EmailVerificationTokenRepository emailVerificationRepository;
	private final JwtService jwtService;
	private final TokenService tokenService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final EmailPublisher emailPublisher;
	
	@Value("${app.frontend.verify-url}")
    private String verifyBaseUrl;
	
	/**
     * Inscrit un nouvel utilisateur dans le système.
     *
     * @param request RegisterRequest contenant :
     *                - email : email de l'utilisateur
     *                - password : mot de passe choisi
     * @return ApiResponse<RegisterResponse>
     *
     * Étapes :
     * 1. Vérifie si l'email est déjà utilisé.
     * 2. Crée un nouvel utilisateur avec rôle USER et mot de passe encodé.
     * 3. Génère un token de vérification d'email.
     * 4. Sauvegarde l'utilisateur et le token dans la base.
     * 5. (TODO) Envoi du token par email à l'utilisateur.
     */
	public ApiResponse<RegisterResponse> register(RegisterRequest request) {
		log.info("Tentative d'inscription pour l'email: {} ...", request.getEmail());
		// Vérifie si l'utilisateur existe déjà
	    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
	    	log.warn("Échec d'inscription: l'email {} est déjà utilisé !", request.getEmail());
	        throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà !");
	    }
	    
	    
	    // Création du nouvel utilisateur
	    User user = User.builder()
				        .email(request.getEmail())
				        .password(passwordEncoder.encode(request.getPassword()))
				        .role(Role.USER)
				        .build();
	    
	    // Génération d'un token de vérification
	    String verificationToken = UUID.randomUUID().toString();
	    EmailVerificationToken emailVerifToken = EmailVerificationToken.builder()
	    		.token(verificationToken)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS)) // 24 heure de validité
                .used(false)
                .build();
	    
	    // Sauvegarde de l'utilisateur dans la base de données
	    userRepository.save(user);
	    // Sauvegarde du token de vérification d'email dans la base de données
	    emailVerificationRepository.save(emailVerifToken);
	    log.info("Nouvel utilisateur créé (non vérifié): {}", user.getEmail());
	    
	    String verificationUrl = verifyBaseUrl + "/" + verificationToken;
	    log.info("URL de vérification Token : {}", verificationUrl);
	    emailPublisher.sendRegistrationEmail(user.getEmail(), verificationToken, verificationUrl);
	    
	    log.info("Token de vérification généré pour {} : {}", user.getEmail(), verificationToken);
	    
	    RegisterResponse response = RegisterResponse.builder()
	            .email(user.getEmail())
	            .message("Inscription réussie ! Un email de vérification vous a été envoyé.")
	            .build();
	    
	    return ApiResponse.<RegisterResponse>builder()
	            .status(201)
	            .success(true)
	            .message("Inscription réussie ! Un email de vérification vous a été envoyé.")
	            .data(response)
	            .build();
	}

	/**
     * Authentifie un utilisateur et génère un couple de tokens (JWT et refresh token).
     *
     * @param request AuthenticationRequest contenant :
     *                - email : email de connexion
     *                - password : mot de passe
     *                - deviceName, userAgent, ipAddress : informations sur le device
     * @return ApiResponse<TokensResponse> contenant :
     *                - accessToken : JWT
     *                - refreshToken : token de rafraîchissement
     *
     * Étapes :
     * 1. Vérifie que l'utilisateur existe.
     * 2. Vérifie si le compte est banni ou non vérifié.
     * 3. Authentifie le mot de passe via AuthenticationManager.
     * 4. Génère un access token et refresh token.
     * 5. Met à jour la dernière connexion et enregistre ou met à jour la session.
     */
    public ApiResponse<TokensResponse> authenticate(AuthenticationRequest request) {
    	log.info("Tentative de connexion pour: {} ...", request.getEmail());
    	
        // Vérifie si l'email existe
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                	log.warn("Aucun utilisateur trouvé pour {} !", request.getEmail());
                	return new IllegalArgumentException("Email ou mot de passe incorrect");
                });

        // Vérifie si le compte est banni
        if (Boolean.TRUE.equals(user.getAccountBanned())) {
        	log.warn("Tentative de connexion d'un compte banni: {} !", request.getEmail());
            throw new IllegalStateException("Ce compte a été banni. Contactez le support.");
        }
        
        // Vérifie si le compte est vérifié
        if (!Boolean.TRUE.equals(user.getVerified())) {
            log.warn("Tentative de connexion d'un compte non vérifié: {} !", request.getEmail());

            // Récupère le dernier token de vérification
            EmailVerificationToken token = emailVerificationRepository
                    .findByUserAndUsedFalse(user)
                    .orElse(null);

            // Si le token est absent ou expiré, on génère un nouveau token et on envoie l'email
            if (token == null || token.isExpired()) {
            	if (token != null) {
                    // Supprime le token expiré
                    emailVerificationRepository.delete(token);
                    log.info("Ancien token expiré supprimé pour {}", user.getEmail());
                }
            	
                String newToken = UUID.randomUUID().toString();
                EmailVerificationToken newEmailToken = EmailVerificationToken.builder()
                        .token(newToken)
                        .user(user)
                        .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                        .used(false)
                        .build();
                
                emailVerificationRepository.save(newEmailToken);

                // TODO : envoi du mail de vérification avec newToken via EmailService
                
                log.info("Nouveau token de vérification généré et envoyé à {} : {}", user.getEmail(), newToken);

                throw new IllegalStateException("Votre email n'est pas vérifié. Un nouveau mail de vérification a été envoyé.");
            }

            // Sinon, le token est encore valide mais l'utilisateur n'a pas cliqué dessus
            throw new IllegalStateException("Veuillez vérifier votre email avant de vous connecter.");
        }
        
        // Authentifier le mot de passe via AuthenticationManager
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
        	log.warn("Échec d'authentification pour {}: Email ou mot de passe incorrect.", request.getEmail());
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }
        
        // Génération les Tokens
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.debug("Tokens générés pour {} !", user.getEmail());
        
        // Mis à jour de la dernière connexion
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        log.info("Dernière connexion mise à jour pour {} !", user.getEmail());
        
        Session session = sessionRepository
        		.findFirstByUserAndDeviceNameAndIsRevokedFalseOrderByLastUsedAtDesc(user, request.getDeviceName())
        		.orElse(Session.builder()
		        		.user(user)
		        		.deviceName(request.getDeviceName())
		        		.userAgent(request.getUserAgent())
		        		.ipAddress(request.getIpAddress())
		        		.build());
        
        session.setRefreshTokenHash(tokenService.hashToken(refreshToken));
        session.setLastUsedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setRevoked(false);
        sessionRepository.save(session);
        log.info("Session enregistrée pour l'utilisateur {} depuis le device: {} !", 
	            user.getEmail(), request.getDeviceName());

        TokensResponse tokens = TokensResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(86400)
                .tokenType("Bearer")
                .build();
        
        return ApiResponse.<TokensResponse>builder()
                .status(200)
                .success(true)
                .message("Authentification réussie")
                .data(tokens)
                .build();
    }
    
    /**
     * Rafraîchit un JWT via un refresh token.
     *
     * @param request RefreshTokenRequest contenant :
     *                - refreshToken : token de rafraîchissement
     *                - deviceName, userAgent, ipAddress : informations sur le device
     * @return ApiResponse<TokensResponse> contenant :
     *                - accessToken : nouveau JWT
     *                - refreshToken : nouveau refresh token
     *
     * Étapes :
     * 1. Vérifie que l'utilisateur existe.
     * 2. Vérifie que la session correspondante existe et que le refresh token est valide.
     * 3. Vérifie la cohérence device/userAgent/ip.
     * 4. Génère de nouveaux tokens et met à jour la session.
     */
    public ApiResponse<TokensResponse> refreshToken(RefreshTokenRequest request) {
    	log.info("Tentative de rafraîchissement de token pour le device: {} ...", request.getDeviceName());
    	
    	// Verifie si l'utilisateur existe
    	String username = jwtService.extractUsername(request.getRefreshToken());
    	User user = userRepository.findByEmail(username)
    	    .orElseThrow(() -> {
    	    	log.warn("Aucun utilisateur trouvé pour {}", username);
    	    	return new IllegalArgumentException("Utilisateur introuvable");
    	    });

    	// Vérifie si le refresh token lié à l'utilisateur est valide ou non
    	Session session = sessionRepository
    		    .findByUserAndIsRevokedFalse(user)
    		    .stream()
    		    .filter(s -> tokenService.matches(request.getRefreshToken(), s.getRefreshTokenHash()))
    		    .findFirst()
    		    .orElseThrow(() -> {
    		    	log.warn("Refresh token invalide pour {} !", user.getEmail());
    		    	return new IllegalArgumentException("Refresh token invalide.");
    		    });

    	// Vérifie si le refresh token est expiré
    	if (session.isExpired()) {
    		log.warn("Session expirée pour {} !", user.getEmail());
    		session.revoke();
    		sessionRepository.save(session);
    	    throw new IllegalStateException("Session expiré. Veuillez vous reconnecter.");
    	}
    	
    	// Vérifie la cohérence du device et de l'ip
    	if (!session.getIpAddress().equals(request.getIpAddress()) ||
			!session.getUserAgent().equals(request.getUserAgent()) ||
			!session.getDeviceName().equals(request.getDeviceName())) {
    		log.error("Incohérence détectée entre la session et l'environnement actuel pour {}", user.getEmail());
    		throw new SecurityException("Incohérence détectée entre la session et l'environnement actuel.");
    	}

    	// Génération les Tokens
    	String newRefreshToken = jwtService.generateRefreshToken(user);
    	String newAccessToken = jwtService.generateToken(user);
    	log.debug("Nouveaux tokens générés pour {} !", user.getEmail());

    	session.setRefreshTokenHash(tokenService.hashToken(newRefreshToken));
    	session.setLastUsedAt(LocalDateTime.now());
    	// Persistance de la nouvelle session
    	sessionRepository.save(session);
    	log.info("Session rafraîchie avec succès pour {} !", user.getEmail());

    	TokensResponse tokens = TokensResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(86400)
                .tokenType("Bearer")
                .build();
        
        return ApiResponse.<TokensResponse>builder()
                .status(200)
                .success(true)
                .message("Token rafraîchi avec succès")
                .data(tokens)
                .build();
    }
    
    /**
     * Déconnecte un utilisateur pour un device spécifique.
     *
     * @param request RefreshTokenRequest contenant :
     *                - refreshToken : token de rafraîchissement
     *                - deviceName, userAgent, ipAddress : informations sur le device
     * @return ApiResponse<Void>
     *
     * Étapes :
     * 1. Vérifie que l'utilisateur existe.
     * 2. Cherche la session correspondant au refresh token et device.
     * 3. Révoque la session et la sauvegarde.
     */
    public ApiResponse<Void> logout(RefreshTokenRequest request) {
    	log.info("Tentative de déconnexion pour le device: {} ...", request.getDeviceName());
    	// Verifie si l'utilisateur existe
    	String username = jwtService.extractUsername(request.getRefreshToken());
    	User user = userRepository.findByEmail(username)
        	    .orElseThrow(() -> {
        	    	log.warn("Aucun utilisateur trouvé pour {}", username);
        	    	return new IllegalArgumentException("Utilisateur introuvable");
        	    });
    	
    	sessionRepository.findByUserAndIsRevokedFalse(user)
    		.stream()
    		.filter(s -> tokenService.matches(request.getRefreshToken(), s.getRefreshTokenHash())
    				&& s.getDeviceName().equals(request.getDeviceName()))
    		.findFirst()
    		.ifPresent(session -> {
    			session.revoke();
    			sessionRepository.save(session);
    			log.info("Session révoquée pour l'utilisateur {} sur le device {} !", user.getEmail(), request.getDeviceName());
    		});
    	
    	return ApiResponse.<Void>builder()
    	        .status(200)
                .success(true)
                .message("Déconnexion réussie")
                .build();
    }
    
    /**
     * Vérifie l'email d'un utilisateur via un token reçu par email.
     *
     * @param request VerifyEmailRequest contenant :
     *                - verifyEmailToken : token de vérification
     * @return ApiResponse<Void>
     *
     * Étapes :
     * 1. Récupère le token valide (non utilisé et existant).
     * 2. Vérifie que le token n'est pas expiré.
     * 3. Vérifie que l'utilisateur n'est pas déjà vérifié et n'est pas banni.
     * 4. Marque l'utilisateur comme vérifié.
     * 5. Marque le token comme utilisé pour éviter toute réutilisation.
     */
    public ApiResponse<Void> verifyEmail(VerifyEmailRequest request) {
    	String token = request.getVerifyEmailToken();
    	log.info("Début de la vérification de l'email avec le token : {}", token);
    	
    	// Récupération du token et vérification qu'il n'est pas déjà utilisé
    	EmailVerificationToken emailVerificationToken = emailVerificationRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> {
                	log.warn("Token de vérification d'email invalide ou déjà utilisé : {}", token);
                	return new IllegalArgumentException("Token invalide ou expiré");
                });
		
    	// Vérification si le token est expiré
		if (emailVerificationToken.isExpired()) {
			log.warn("Token expiré pour l'utilisateur {} (token : {})", emailVerificationToken.getUser().getEmail(), emailVerificationToken.getToken());
            throw new IllegalStateException("Token expiré");
        }
		
		User user = emailVerificationToken.getUser();
		
		// Vérification si l'utilisateur est déjà vérifié
	    if (Boolean.TRUE.equals(user.getVerified())) {
	        log.warn("L'utilisateur {} est déjà vérifié", user.getEmail());
	        throw new IllegalStateException("Email déjà vérifié");
	    }
	    
	    // 4. Vérification si le compte est banni
	    if (Boolean.TRUE.equals(user.getAccountBanned())) {
	        log.warn("Tentative de vérification d'un compte banni : {}", user.getEmail());
	        throw new IllegalStateException("Impossible de vérifier un compte banni");
	    }
	    
	    // 5. Marque l'utilisateur comme vérifié
        user.setVerified(true);
        userRepository.save(user);
        log.info("Utilisateur {} marqué comme vérifié", user.getEmail());

        // Mise à jour du token comme utilisé
        emailVerificationToken.setUsed(true);
        emailVerificationRepository.save(emailVerificationToken);
        log.info("Token {} marqué comme utilisé pour l'utilisateur {}", token, user.getEmail());

        log.info("Email vérifié pour l'utilisateur {}", user.getEmail());
        
        return ApiResponse.<Void>builder()
                .status(200)
                .success(true)
                .message("Email vérifié avec succès ! Vous pouvez maintenant vous connecter.")
                .build();
    }
    
    public ApiResponse<UserResponse> getCurrentUser(String email) {
        log.info("Début de la récupération d'informations de l'utilisateur : {}" , email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        UserResponse userDto = UserResponse.builder()
                .id(user.getId())
                .authUserId(user.getAuthUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();

        return ApiResponse.<UserResponse>builder()
                .status(200)
                .success(true)
                .message("Profil récupéré")
                .data(userDto)
                .build();
    }
}
