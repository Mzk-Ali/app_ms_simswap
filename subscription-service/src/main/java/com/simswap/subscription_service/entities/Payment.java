package com.simswap.subscription_service.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.simswap.subscription_service.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le userId ne doit pas etre vide")
    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subscription_id")
    private UserSubscription userSubscription;

    @Column(unique = true)
    private String stripePaymentIntentId;

    private String stripeChargeId;
    
    private String stripeInvoiceId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(length = 3)
    private String currency = "EUR";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentMethod;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(columnDefinition = "TEXT")
    private String stripeInvoiceUrl;
    
    @Column(columnDefinition = "TEXT")
    private String stripeInvoicePdfUrl;

    private LocalDateTime paidAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
