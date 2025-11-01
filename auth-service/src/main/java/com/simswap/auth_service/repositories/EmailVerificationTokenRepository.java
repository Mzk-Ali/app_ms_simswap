package com.simswap.auth_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simswap.auth_service.entities.EmailVerificationToken;
import com.simswap.auth_service.entities.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
	Optional<EmailVerificationToken> findByTokenAndUsedFalse(String token);
	Optional<EmailVerificationToken> findByUserAndUsedFalse(User user);
}
