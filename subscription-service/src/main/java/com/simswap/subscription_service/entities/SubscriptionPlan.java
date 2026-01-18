package com.simswap.subscription_service.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "subscription_plans")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, length = 20)
    private String durationType;
    
    private Integer durationValue;
    
    @Column(nullable = false)
    private Integer simswapLimit;

    private String stripePriceId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @PastOrPresent(message = "La date de création ne peut pas être dans le futur")
    private Instant createdAt;
    
    @PastOrPresent(message = "La date de mise à jour ne peut pas être dans le futur")
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
