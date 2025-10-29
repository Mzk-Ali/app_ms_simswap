package com.simswap.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long accessTokenValidity;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenValidity;

    public String getSecret() { return secret; }
    public long getAccessTokenValidity() { return accessTokenValidity; }
    public long getRefreshTokenValidity() { return refreshTokenValidity; }
}
