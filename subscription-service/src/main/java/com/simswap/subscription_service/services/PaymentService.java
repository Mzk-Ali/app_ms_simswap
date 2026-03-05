package com.simswap.subscription_service.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.entities.Payment;
import com.simswap.subscription_service.enums.PaymentStatus;
import com.simswap.subscription_service.repositories.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    
    public ApiResponse<String> getInvoiceUrl(String paymentId) {
        Payment payment = paymentRepository.findById(Long.valueOf(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Paiement non trouvé."));

        if (payment.getStripeInvoiceUrl() != null) {
            return ApiResponse.<String>builder()
                    .status(200)
                    .success(true)
                    .message("")
                    .data(payment.getStripeInvoiceUrl())
                    .build();
        }

        if (payment.getStripeInvoiceId() != null) {
            try {
                Invoice invoice = Invoice.retrieve(payment.getStripeInvoiceId());
                
                payment.setStripeInvoiceUrl(invoice.getHostedInvoiceUrl());
                payment.setStripeInvoicePdfUrl(invoice.getInvoicePdf());
                paymentRepository.save(payment);
                
                return ApiResponse.<String>builder()
                        .status(200)
                        .success(true)
                        .message("")
                        .data(invoice.getHostedInvoiceUrl())
                        .build();
                
            } catch (StripeException e) {
                log.error("Erreur récupération facture Stripe: {}", e.getMessage());
                throw new RuntimeException("Impossible de récupérer la facture");
            }
        }

        throw new IllegalStateException("Aucune facture disponible pour ce paiement");
    }

    public void updatePaymentStatus(String paymentIntentId, PaymentStatus status, String chargeId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement non trouvé"));

        payment.setStatus(status);
        payment.setStripeChargeId(chargeId);
        
        if (status == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);
        log.info("Statut du paiement {} mis à jour: {}", paymentIntentId, status);
    }
}
