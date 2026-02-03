package com.simswap.auth_service.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.simswap.auth_service.entities.User;
import com.simswap.auth_service.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public void deleteAccount(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe incorrect");
        }
        
        userRepository.delete(user);
        
        log.warn("Compte supprime pour: {}", email);
    }
}
