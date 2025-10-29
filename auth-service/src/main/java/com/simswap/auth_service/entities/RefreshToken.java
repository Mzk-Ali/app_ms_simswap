package com.simswap.auth_service.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String refreshToken;
    
    private Boolean isRevoked;

    private Instant expiresAt;
    
    private Instant createdAt;
    
    private Instant revokedAt;

    @ManyToOne
    private User user;
}
