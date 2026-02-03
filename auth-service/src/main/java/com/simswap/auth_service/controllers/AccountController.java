package com.simswap.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.auth_service.dtos.ApiResponse;
import com.simswap.auth_service.dtos.DeleteAccountRequest;
import com.simswap.auth_service.services.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;
    
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        log.warn("Demande de suppression de compte pour : {}", email);
        
        accountService.deleteAccount(email, request.getPassword());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Compte supprime avec succes")
                .build());
    }
}
