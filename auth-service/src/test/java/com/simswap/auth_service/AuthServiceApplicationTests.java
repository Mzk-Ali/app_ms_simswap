package com.simswap.auth_service;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.simswap.auth_service.services.JwtService;
import com.simswap.auth_service.services.TokenService;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {
	@TestConfiguration
    static class TestConfig {

        @Bean
        public AuthenticationManager authenticationManager() {
            return email -> null;
        }

        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        public TokenService tokenService() {
            return mock(TokenService.class);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }
    }

	@Test
	void contextLoads() {
	}

}
