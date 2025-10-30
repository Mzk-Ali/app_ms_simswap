package com.simswap.auth_service.dtos;

import lombok.Data;

@Data
public class RefreshTokenRequest {
	private String refreshToken;
    private String deviceName;
    private String userAgent;
    private String ipAddress;
}
