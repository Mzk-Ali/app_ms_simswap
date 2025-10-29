package com.simswap.auth_service.dtos;

import lombok.Data;

@Data
public class RegisterRequest {
	private String email;
    private String password;
}
