package com.simswap.auth_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format d'email est invalide")
    private String email;
    
    @NotBlank
    private String password;
    
    private String deviceName;
    private String userAgent;
    private String ipAddress;
}
