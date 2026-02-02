package com.simswap.subscription_service.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.simswap.subscription_service.dtos.CheckoutSessionRequest;
import com.simswap.subscription_service.dtos.CheckoutSessionResponse;
import com.simswap.subscription_service.entities.Payment;
import com.simswap.subscription_service.entities.SubscriptionPlan;
import com.simswap.subscription_service.entities.UserSubscription;
import com.simswap.subscription_service.enums.PaymentStatus;
import com.simswap.subscription_service.repositories.PaymentRepository;
import com.simswap.subscription_service.repositories.SubscriptionPlanRepository;
import com.simswap.subscription_service.repositories.UserSubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    public CheckoutSessionResponse createCheckoutSession(CheckoutSessionRequest request)
    {
        Long planId = request.getPlanId();
        Long subscriptionId = request.getSubscriptionId();
        
        try {
            SubscriptionPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Plan non trouvé"));
            
            UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Abonnement non trouvé"));

            Payment payment = Payment.builder()
                    .userId(request.getUserId())
                    .userSubscription(subscription)
                    .amount(plan.getPriceAmount())
                    .currency(plan.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .description("Abonnement " + plan.getName())
                    .build();

            payment = paymentRepository.save(payment);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", request.getUserId());
            metadata.put("planId", request.getPlanId().toString());
            metadata.put("paymentId", payment.getId().toString());
            if (request.getSubscriptionId() != null) {
                metadata.put("subscriptionId", request.getSubscriptionId().toString());
            }
            
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(plan.getStripePriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .build();
            
            Session session = Session.create(params);

            log.info("Checkout session créée: {} pour payment: {}", session.getId(), payment.getId());

            return CheckoutSessionResponse.builder()
                    .sessionId(session.getId())
                    .url(session.getUrl())
                    .paymentId(payment.getId().toString())
                    .build();
        } catch (StripeException e) {
            log.error("Erreur création checkout session: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de la session de paiement: " + e.getMessage());
        }
    }
}
