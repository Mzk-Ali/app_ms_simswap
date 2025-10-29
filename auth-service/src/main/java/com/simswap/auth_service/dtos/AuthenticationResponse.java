package com.simswap.auth_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
	private String accessToken;
    private String refreshToken;
}
