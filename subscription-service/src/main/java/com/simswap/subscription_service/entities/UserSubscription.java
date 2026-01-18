package com.simswap.subscription_service.entities;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.simswap.subscription_service.enums.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user_subscriptions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le userId ne doit pas etre vide")
    @Column(nullable = false)
    private String userId;
    
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubscriptionStatus status;
    
    @Column(unique = true)
    private String stripeSubscriptionId;

    private String stripeCustomerId;
    
    @Column(nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(nullable = false)
    private LocalDateTime currentPeriodEnd;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean cancelAtPeriodEnd = false;
    
    private LocalDateTime cancelledAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
