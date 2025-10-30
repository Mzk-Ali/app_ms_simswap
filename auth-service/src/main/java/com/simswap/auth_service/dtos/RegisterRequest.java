package com.simswap.auth_service.dtos;

import com.simswap.auth_service.entities.Role;

import lombok.Data;

@Data
public class RegisterRequest {
	private String email;
    private String password;
    private Role role;
}
