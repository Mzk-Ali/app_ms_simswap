package com.simswap.subscription_service.fallback;

import org.springframework.stereotype.Component;

import com.simswap.subscription_service.dtos.CheckoutSessionRequest;
import com.simswap.subscription_service.dtos.CheckoutSessionResponse;
import com.simswap.subscription_service.feignclient.PaymentServiceClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentServiceClientFallback implements PaymentServiceClient {
    @Override
    public CheckoutSessionResponse createCheckoutSession(CheckoutSessionRequest request) {
        log.error("Fallback: Impossible de créer la session de paiement pour user {}", request.getUserId());
        throw new RuntimeException("Service de paiement temporairement indisponible");
    }
}
