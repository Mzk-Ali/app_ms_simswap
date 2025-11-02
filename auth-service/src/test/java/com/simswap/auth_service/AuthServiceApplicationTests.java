package com.simswap.auth_service;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.simswap.auth_service.repositories.UserRepository;
import com.simswap.auth_service.services.JwtService;
import com.simswap.auth_service.services.TokenService;

@SpringBootTest
@EnableAutoConfiguration(exclude = { 
	    DataSourceAutoConfiguration.class, 
	    HibernateJpaAutoConfiguration.class 
	})
@ActiveProfiles("test")
class AuthServiceApplicationTests {
	@TestConfiguration
    static class TestConfig {

        @Bean
        public AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
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

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }

	@Test
	void contextLoads() {
	}

}
