package com.simswap.subscription_service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.simswap.subscription_service.dtos.CheckoutSessionRequest;
import com.simswap.subscription_service.dtos.CheckoutSessionResponse;
import com.simswap.subscription_service.fallback.PaymentServiceClientFallback;

@FeignClient(
        name = "payment-service",
        url = "${services.payment.url}",
        fallback = PaymentServiceClientFallback.class
    )
public interface PaymentServiceClient {
    @PostMapping("/api/payments/create-checkout-session")
    CheckoutSessionResponse createCheckoutSession(@RequestBody CheckoutSessionRequest request);
}
