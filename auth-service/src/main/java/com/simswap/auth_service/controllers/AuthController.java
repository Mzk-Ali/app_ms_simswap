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
	

    @PostMapping("/register")
    public ResponseEntity<TokensResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokensResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokensResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
    	authService.logout(request);
    	return ResponseEntity.ok().build();
    }
}
