package com.simswap.subscription_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.services.SubscriptionWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/subscriptions/webhook")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionWebhookController {
    private final SubscriptionWebhookService webhookService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @PostMapping("/stripe")
    public ResponseEntity<ApiResponse<Void>> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader)
    {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("La verification du Webhook signature a échouée : {}", e.getMessage());
            throw new IllegalArgumentException("Signature invalide !");
        }

        try {
            webhookService.handleStripeEvent(event);
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .message("Webhook handled successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(500)
                .success(false)
                .message("Erreur lors du processus Webhook")
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
