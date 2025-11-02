package com.simswap.auth_service.entities;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session  {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NotNull(message = "L'utilisateur associé à la session est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
	
	@NotBlank(message = "Le nom du device est obligatoire")
    @Length(max = 100, message = "Le nom du device ne doit pas dépasser 100 caractères")
    @Column(name = "device_name", length = 100, nullable = false)
	private String deviceName;
	
	@NotBlank(message = "L'adresse IP est obligatoire")
    @Pattern(
        regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^[a-fA-F0-9:]+$",
        message = "Adresse IP invalide (IPv4 ou IPv6 attendue)"
    )
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
	
	@NotBlank(message = "Le User-Agent est obligatoire")
    @Size(max = 1024, message = "Le User-Agent ne doit pas dépasser 1024 caractères")
    @Column(name = "user_agent", columnDefinition = "TEXT", nullable = false)
    private String userAgent; // Navigateur ou App client

	@NotBlank(message = "Le hash du refresh token est obligatoire")
    @Column(name = "refresh_token_hash", nullable = false, length = 255)
    private String refreshTokenHash;

    @Builder.Default
    @PastOrPresent(message = "La date de création ne peut pas être dans le futur")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PastOrPresent(message = "La dernière utilisation ne peut pas être dans le futur")
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Future(message = "La date d'expiration doit être dans le futur")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PastOrPresent(message = "La date de révocation ne peut pas être dans le futur")
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Builder.Default
    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false;
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
