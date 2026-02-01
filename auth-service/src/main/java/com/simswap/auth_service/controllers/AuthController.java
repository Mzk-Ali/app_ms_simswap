package com.simswap.auth_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.auth_service.dtos.ApiResponse;
import com.simswap.auth_service.dtos.AuthenticationRequest;
import com.simswap.auth_service.dtos.RefreshTokenRequest;
import com.simswap.auth_service.dtos.RegisterRequest;
import com.simswap.auth_service.dtos.RegisterResponse;
import com.simswap.auth_service.dtos.TokensResponse;
import com.simswap.auth_service.dtos.UserResponse;
import com.simswap.auth_service.dtos.VerifyEmailRequest;
import com.simswap.auth_service.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication", description = "Endpoints pour la gestion de l'authentification, inscription et sessions utilisateur")
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
     * @return ResponseEntity<RegisterResponse> : HTTP 200 OK si succès
     * 
     * @throws IllegalArgumentException si l'utilisateur existe déjà
     */
    @PostMapping("/register")
    @Operation(
	    summary = "Inscription d'un nouvel utilisateur",
		description = """
	    	Crée un nouvel utilisateur non vérifié et génère un token de vérification envoyé par email
	        
	    	Fonctionnement :
	    		- Vérifie que l'email fourni n'est pas déjà utilisé
	    		- Création d'un utilisateur avec le rôle USER et mot de passe encodé
	    		- Génération d’un token de vérification d’email (valide 24h)
	    		- Sauvegarde de l’utilisateur et du token dans la base
	    		- Envoi du token de vérification par email à l'utilisateur
	        
	        Remarques importantes :
	    		- L'utilisateur créé n'est pas encore vérifié : il doit valider son email via le token reçu
	    		- Une erreur est renvoyée si l'email est déjà utilisé
	    		- Aucun JWT ou authentification n’est nécessaire pour cette opération
	    """
	)
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<RegisterResponse> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    @Operation(
	    summary = "Authentification d'un utilisateur",
		description = """
	    	Authentifie un utilisateur et génère un couple de tokens (JWT + refresh token), tout en gérant la validation du compte et la création/mise à jour de la session.
	        
	        Fonctionnement :
	    		- Vérifie que l'utilisateur existe via son email
	    		- Vérifie que le compte n'est pas banni
	    		- Vérifie que l'email de l'utilisateur est validé :
                           - si non vérifié mais token valide : accès refusé
                           - si non vérifié et token expiré/absent : génération d’un nouveau token et renvoi de l’email
	    		- Vérifie la validité du mot de passe
	    		- Génère un access token (JWT) et un refresh token
	    		- Met à jour la dernière connexion de l'utilisateur
	    		- Crée ou met à jour la session liée au device, incluant :
                           - hash du refresh token
                           - user-agent
                           - adresse IP
                           - expiration de la session
                           - statut de révocation
	        
	        Remarques :
	    		- Une erreur est renvoyée si l'utilisateur est non vérifié : un email de vérification peut être renvoyé automatiquement
	    		- Un compte banni ne peut jamais s'authentifier
	    		- Chaque device possède sa propre session, gérant séparément les refresh tokens
	    		- L’endpoint renvoie un JWT + un refresh token si l’authentification réussit
	    """
	)
    public ResponseEntity<ApiResponse<TokensResponse>> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        ApiResponse<TokensResponse> response = authService.authenticate(request);
        
        return ResponseEntity.ok(response);
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
    @Operation(
	    summary = "Renouvellement du token d'authentification (Refresh Token)",
	    description = """
	    	Permet de générer un nouveau couple de tokens (accessToken + refreshToken) à partir d'un refresh token valide
	        
	        Le processus de sécurité inclut :
	    		- Vérification que le refresh token appartient bien à un utilisateur existant
	    		- Vérification que la session associée au refresh token n'est pas révoquée
	    		- Vérification que le refresh token n'est pas expiré
	    		- Vérification de la cohérence de l'environnement de connexion :
	    				- deviceName identique
	    				- userAgent identique
	    				- ipAddress identique
	    		- Génération d'un nouveau refresh token et access token
	    		- Mise à jour de la session (nouveau hash du refresh token, mise à jour `lastUsedAt`, ...)
	        
	        Remarques :
	    		- En cas de refresh token expiré, la session est automatiquement révoquée et l'utilisateur doit se reconnecter
	    		- En cas d'incohérence device/userAgent/IP, une erreur de sécurité est renvoyée
	    """
	)
    public ResponseEntity<ApiResponse<TokensResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        ApiResponse<TokensResponse> response = authService.refreshToken(request);
        
        return ResponseEntity.ok(response);
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
    @Operation(
	    summary = "Vérification de l'email d'un utilisateur après inscription",
	    description = """
	    	Permet de valider l'adresse email d'un utilisateur à l'aide d'un token de vérification envoyé par email
	        
	        Fonctionnement :
	    		- Récupération du token transmis dans la requête
	    		- Vérification que le token existe et n'a pas déjà été utilisé
	    		- Contrôle que le token n'est pas expiré
	    		- Vérification de l'état du compte associé :
	    		           - utilisateur non déjà vérifié
	    		           - utilisateur non banni
	    		- Marque l'utilisateur comme vérifié
	    		- Marque le token comme utilisé pour empêcher toute réutilisation
		        
	        Remarques importantes :
	    		- Le token doit être valide, non expiré et non utilisé
	    		- Si l'utilisateur est déjà vérifié, une erreur est renvoyée
	    		- Il est impossible de valider un compte banni
	    		- Aucune authentification JWT n'est nécessaire pour cette opération
	    """
	)
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        ApiResponse<Void> response = authService.logout(request);
        
        return ResponseEntity.ok(response);
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
    @Operation(
	    summary = "Vérification d'un compte utilisateur après une inscription",
	    description = """
	    	Permet de déconnecter un utilisateur en révoquant uniquement la session associée au refresh token et au device fourni
	        
	        Fonctionnement :
	    		- Extraction de l'email depuis le refresh token
	    		- Vérification que l'utilisateur existe
	    		- Recherche de la session active associée :
	    				au refresh token fourni
	    				au deviceName fourni
	    		- Révocation de la session (marquée comme 'revoked')
	    		- Sauvegarde de la session mise à jour
		        
	        Remarques importantes :
	    		- Cette opération ne nécessite pas de JWT actif, uniquement un refresh token valide
	    		- Si aucune session correspondant au refresh token et au device n'est trouvée, l'opération réussit silencieusement (aucune erreur n'est renvoyée)
	    		- Seule la session du device concerné est invalidée, les autres restent actives
	    """
	)
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        ApiResponse<Void> response = authService.verifyEmail(request);
        
        return ResponseEntity.ok(response);
    }
    
    
    
    @GetMapping("/me")
    @Operation(summary = "Récupérer les informations de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@RequestHeader("X-User-Email") String email) {
        ApiResponse<UserResponse> response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }
}
