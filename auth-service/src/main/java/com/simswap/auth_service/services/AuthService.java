package com.simswap.auth_service.services;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.simswap.auth_service.dtos.AuthenticationRequest;
import com.simswap.auth_service.dtos.RefreshTokenRequest;
import com.simswap.auth_service.dtos.RegisterRequest;
import com.simswap.auth_service.dtos.TokensResponse;
import com.simswap.auth_service.entities.Role;
import com.simswap.auth_service.entities.Session;
import com.simswap.auth_service.entities.User;
import com.simswap.auth_service.repositories.UserRepository;
import com.simswap.auth_service.repositories.sessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final sessionRepository sessionRepository;
	private final JwtService jwtService;
	private final TokenService tokenService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	
	public TokensResponse register(RegisterRequest request) {
		// Vérifie si l'utilisateur existe déjà
	    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
	        throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà !");
	    }
	    
	    // Création du nouvel utilisateur
	    User user = User.builder()
				        .email(request.getEmail())
				        .password(passwordEncoder.encode(request.getPassword()))
				        .role(Role.USER)
				        .build();
	    // Sauvegarde de l'utilisateur dans la base de données
	    userRepository.save(user);
	    
	    // Génération les Tokens
	    String jwtToken = jwtService.generateToken(user);
	    String refreshToken = jwtService.generateRefreshToken(user);
	    
	    // Réponse vers le Client
	    return TokensResponse.builder()
	        .accessToken(jwtToken)
            .refreshToken(refreshToken)
	        .build();
	}

    public TokensResponse authenticate(AuthenticationRequest request) {
        // Vérifie si l'email existe
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur trouvé avec cet email"));

        // Vérifie si le compte est banni
        if (Boolean.TRUE.equals(user.getAccountBanned())) {
            throw new IllegalStateException("Ce compte a été banni. Contactez le support.");
        }
        
        // Vérifie si le compte est vérifié
        if (!Boolean.TRUE.equals(user.getVerified())) {
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
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }
        
//        var user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow();
        
        // Génération les Tokens
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Mis à jour de la dernière connexion
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        
        Session session = sessionRepository
        		.findByUserAndDeviceName(user, request.getDeviceName())
        		.filter(s -> !s.isRevoked())
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
//        revokeAllUserTokens(user);
//        saveUserToken(user, jwtToken);
        return TokensResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    public TokensResponse refreshToken(RefreshTokenRequest request) {
    	// Verifie si l'utilisateur existe
    	String username = jwtService.extractUsername(request.getRefreshToken());
    	User user = userRepository.findByEmail(username)
    	    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

    	// Vérifie si le refresh token lié à l'utilisateur est valide ou non
    	Session session = sessionRepository
    		    .findByUserAndIsRevokedFalse(user)
    		    .stream()
    		    .filter(s -> tokenService.matches(request.getRefreshToken(), s.getRefreshTokenHash()))
    		    .findFirst()
    		    .orElseThrow(() -> new IllegalArgumentException("Refresh token invalide."));

    	// Vérifie si le refresh token est expiré
    	if (session.isExpired()) {
    		session.revoke();
    		sessionRepository.save(session);
    	    throw new IllegalStateException("Session expiré. Veuillez vous reconnecter.");
    	}
    	
    	// Vérifie la cohérence du device et de l'ip
    	if (!session.getIpAddress().equals(request.getIpAddress()) ||
			!session.getUserAgent().equals(request.getUserAgent()) ||
			!session.getDeviceName().equals(request.getDeviceName())) {
    		throw new SecurityException("Incohérence détectée entre la session et l'environnement actuel.");
    	}

    	// Génération les Tokens
    	String newRefreshToken = jwtService.generateRefreshToken(user);
    	String newAccessToken = jwtService.generateToken(user);

    	session.setRefreshTokenHash(tokenService.hashToken(newRefreshToken));
    	session.setLastUsedAt(LocalDateTime.now());
    	// Persistance de la nouvelle session
    	sessionRepository.save(session);

    	return TokensResponse.builder()
    		    .accessToken(newAccessToken)
    		    .refreshToken(newRefreshToken)
    		    .build();
    }
    
    public void logout(RefreshTokenRequest request) {
    	// Verifie si l'utilisateur existe
    	String username = jwtService.extractUsername(request.getRefreshToken());
    	User user = userRepository.findByEmail(username)
        	    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    	
    	sessionRepository.findByUserAndIsRevokedFalse(user)
    		.stream()
    		.filter(s -> tokenService.matches(request.getRefreshToken(), s.getRefreshTokenHash())
    				&& s.getDeviceName().equals(request.getDeviceName()))
    		.findFirst()
    		.ifPresent(session -> {
    			session.revoke();
    			sessionRepository.save(session);
    		});
    }
}
