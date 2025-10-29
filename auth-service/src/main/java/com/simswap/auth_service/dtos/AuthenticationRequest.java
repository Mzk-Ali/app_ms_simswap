package com.simswap.auth_service.dtos;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
}
