package com.simswap.subscription_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String userId;
    private String subscriptionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private String stripeInvoiceId;
    private String stripeInvoiceUrl;
    private String stripeInvoicePdfUrl;
    private String paymentMethod;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
