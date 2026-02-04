package com.simswap.auth_service.repositories;

import org.springframework.stereotype.Repository;

import com.simswap.auth_service.entities.Session;
import com.simswap.auth_service.entities.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
	/**
     * Trouve toutes les sessions actives (non révoquées) d’un utilisateur
     */
    List<Session> findByUserAndIsRevokedFalse(User user);
	
	/**
     * Trouve une session d’un utilisateur et d'un DeviceName
     */
    Optional<Session> findByUserAndDeviceName(User user, String deviceName);
    
	/**
     * Trouve une session précise par refresh token hash
     */
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);
    
    /**
     * Supprime toutes les sessions expirées (optionnel, utile pour un batch cleanup)
     */
    void deleteByExpiresAtBefore(java.time.LocalDateTime dateTime);
    
    Optional<Session> findFirstByUserAndDeviceNameAndIsRevokedFalseOrderByLastUsedAtDesc(User user, String deviceName);
}
