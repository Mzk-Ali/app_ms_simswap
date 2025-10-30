package com.simswap.auth_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TokensResponse {
	private String accessToken;
    private String refreshToken;
}
