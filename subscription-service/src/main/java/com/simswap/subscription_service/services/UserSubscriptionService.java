package com.simswap.subscription_service.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.dtos.CheckoutSessionRequest;
import com.simswap.subscription_service.dtos.CheckoutSessionResponse;
import com.simswap.subscription_service.dtos.SubscribeRequest;
import com.simswap.subscription_service.dtos.SubscribeResponse;
import com.simswap.subscription_service.dtos.SubscriptionPlanResponse;
import com.simswap.subscription_service.dtos.SubscriptionValidationResponse;
import com.simswap.subscription_service.dtos.UserSubscriptionResponse;
import com.simswap.subscription_service.entities.SubscriptionPlan;
import com.simswap.subscription_service.entities.UserSubscription;
import com.simswap.subscription_service.enums.SubscriptionStatus;
import com.simswap.subscription_service.repositories.SubscriptionPlanRepository;
import com.simswap.subscription_service.repositories.UserSubscriptionRepository;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.param.SubscriptionUpdateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {
    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final StripeService stripeService;

    
    public ApiResponse<UserSubscriptionResponse> getActiveSubscription(String userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .map(sub -> {
                SubscriptionPlan planEntity = sub.getPlan();
                SubscriptionPlanResponse planDto = SubscriptionPlanResponse.builder()
                        .id(planEntity.getId())
                        .name(planEntity.getName())
                        .code(planEntity.getCode())
                        .price(planEntity.getPriceAmount())
                        .currency(planEntity.getCurrency())
                        .simswapLimit(planEntity.getSimswapLimit())
                        .build();

                UserSubscriptionResponse data = UserSubscriptionResponse.builder()
                        .id(String.valueOf(sub.getId()))
                        .userId(sub.getUserId())
                        .status(sub.getStatus().name())
                        .plan(planDto)
                        .currentPeriodStart(sub.getCurrentPeriodStart())
                        .currentPeriodEnd(sub.getCurrentPeriodEnd())
                        .cancelAtPeriodEnd(sub.getCancelAtPeriodEnd())
                        .stripeSubscriptionId(sub.getStripeSubscriptionId())
                        .createdAt(sub.getCreatedAt())
                        .build();

                return ApiResponse.<UserSubscriptionResponse>builder()
                        .status(200)
                        .success(true)
                        .message("Abonnement actif trouvé")
                        .data(data)
                        .build();
            })
            .orElseGet(() -> {
                return ApiResponse.<UserSubscriptionResponse>builder()
                        .status(200)
                        .success(true)
                        .message("Aucun abonnement actif trouvé")
                        .data(null)
                        .build();
            });
    }
    
    public ApiResponse<SubscribeResponse> subscribe(String email, SubscribeRequest request) {
        String userId = request.getUserId();
        Long planId = request.getPlanId();

        subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .ifPresent(sub -> {
                    log.error("Vous avez déjà un abonnement actif : {}", sub.getId());
                    throw new IllegalArgumentException("Vous avez déjà un abonnement actif");
                });

        // Récupérer le plan
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan non trouvé"));

        if (!plan.getIsActive()) {
            throw new IllegalArgumentException("Ce plan n'est plus disponible");
        }

        // Créer l'abonnement en statut PENDING
        UserSubscription subscription = UserSubscription.builder()
                .userId(userId)
                .plan(plan)
                .status(SubscriptionStatus.PENDING)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(calculatePeriodEnd(plan))
                .cancelAtPeriodEnd(false)
                .build();

        subscription = subscriptionRepository.save(subscription);

        CheckoutSessionResponse checkoutSession = stripeService.createCheckoutSession(
                CheckoutSessionRequest.builder()
                        .userId(userId.toString())
                        .planId(planId)
                        .subscriptionId(subscription.getId())
                        .build()
        );

        SubscribeResponse response = SubscribeResponse.builder()
                .subscriptionId(subscription.getId())
                .checkoutUrl(checkoutSession.getUrl())
                .sessionId(checkoutSession.getSessionId())
                .build();
        
        return ApiResponse.<SubscribeResponse>builder()
                .status(200)
                .success(true)
                .message("Session de paiement créée avec succès")
                .data(response)
                .build();
    }
    
    
    
    public ApiResponse<UserSubscriptionResponse> cancelSubscription(Long subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Abonnement non trouvé"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalArgumentException("Seuls les abonnements actifs peuvent être annulés");
        }

        subscription.setCancelAtPeriodEnd(true);

        if (subscription.getStripeSubscriptionId() != null) {
            try {
                Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                stripeSubscription.update(params);
            } catch (Exception e) {
                log.error("Erreur mise à jour Stripe: {}", e.getMessage());
            }
        }

        subscription = subscriptionRepository.save(subscription);
        log.info("Abonnement {} annulé", subscriptionId);
        
        UserSubscriptionResponse response = UserSubscriptionResponse.builder()
                .status(subscription.getStatus().name())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .build();
        
        return ApiResponse.<UserSubscriptionResponse>builder()
                .status(200)
                .success(true)
                .message("Abonnement marqué pour annulation à la fin de la période")
                .data(response)
                .build();
    }
    
    public ApiResponse<SubscriptionValidationResponse> validateSubscription(String userId) {
        UserSubscription subscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId)
                .orElse(null);
        
        SubscriptionValidationResponse response;

        if (subscription == null || subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            response = SubscriptionValidationResponse.builder()
                    .userId(userId)
                    .isActive(false)
                    .canUseService(false)
                    .subscriptionStatus("NONE")
                    .limit(0)
                    .build();
        } else {
            response = SubscriptionValidationResponse.builder()
                    .userId(userId)
                    .isActive(true)
                    .canUseService(true)
                    .subscriptionStatus(subscription.getStatus().name())
                    .limit(subscription.getPlan().getSimswapLimit())
                    .periodEnd(subscription.getCurrentPeriodEnd())
                    .planCode(subscription.getPlan().getCode())
                    .build();
        }

        
        return ApiResponse.<SubscriptionValidationResponse>builder()
                .status(200)
                .success(true)
                .message("Vérification de l'abonnement terminée")
                .data(response)
                .build();
    }
    
    public ApiResponse<UserSubscriptionResponse> syncWithStripe(String subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findById(Long.valueOf(subscriptionId))
                .orElseThrow(() -> new IllegalArgumentException("Abonnement non trouvé"));

        if (subscription.getStripeSubscriptionId() == null) {
            throw new IllegalArgumentException("Cet abonnement n'est pas lié à Stripe");
        }

        try {
            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());
            
            if (stripeSubscription.getItems() != null && 
                stripeSubscription.getItems().getData() != null &&
                !stripeSubscription.getItems().getData().isEmpty()) {
                
                SubscriptionItem firstItem = stripeSubscription.getItems().getData().get(0);
                
                if (firstItem.getCurrentPeriodStart() != null) {
                    subscription.setCurrentPeriodStart(
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(firstItem.getCurrentPeriodStart()),
                                    ZoneId.systemDefault()
                            )
                    );
                }
                
                if (firstItem.getCurrentPeriodEnd() != null) {
                    subscription.setCurrentPeriodEnd(
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(firstItem.getCurrentPeriodEnd()),
                                    ZoneId.systemDefault()
                            )
                    );
                }
            }
            
            SubscriptionStatus newStatus = mapStripeStatusToOurStatus(stripeSubscription.getStatus());
            subscription.setStatus(newStatus);
            
            Boolean cancelAtPeriodEnd = stripeSubscription.getCancelAtPeriodEnd();
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd != null ? cancelAtPeriodEnd : false);
            
            subscriptionRepository.save(subscription);
            log.info("Abonnement {} synchronisé avec Stripe", subscriptionId);
            
        } catch (Exception e) {
            log.error("Erreur synchronisation Stripe: {}", e.getMessage());
            throw new IllegalStateException("Erreur lors de la synchronisation avec Stripe");
        }

        UserSubscriptionResponse response = UserSubscriptionResponse.builder()
                .status(subscription.getStatus().name())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .build();
        
        return ApiResponse.<UserSubscriptionResponse>builder()
                .status(200)
                .success(true)
                .message("Abonnement synchronisé avec Stripe")
                .data(response)
                .build();
    }
    
    
    public ApiResponse<Void> activateSubscription(String subscriptionId, String stripeSubscriptionId, String stripeCustomerId) {
        UserSubscription subscription = subscriptionRepository.findById(Long.valueOf(subscriptionId))
                .orElseThrow(() -> new IllegalArgumentException("Abonnement non trouvé"));

        String previousStatus = subscription.getStatus().name();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscription.setStripeCustomerId(stripeCustomerId);

        try {
            Subscription stripeSubscription = Subscription.retrieve(stripeSubscriptionId);
            
            if (stripeSubscription.getItems() != null && 
                stripeSubscription.getItems().getData() != null &&
                !stripeSubscription.getItems().getData().isEmpty()) {
                
                SubscriptionItem firstItem = stripeSubscription.getItems().getData().get(0);
                
                if (firstItem.getCurrentPeriodStart() != null) {
                    subscription.setCurrentPeriodStart(
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(firstItem.getCurrentPeriodStart()),
                                    ZoneId.systemDefault()
                            )
                    );
                }
                
                if (firstItem.getCurrentPeriodEnd() != null) {
                    subscription.setCurrentPeriodEnd(
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(firstItem.getCurrentPeriodEnd()),
                                    ZoneId.systemDefault()
                            )
                    );
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur récupération détails Stripe: {}", e.getMessage());
            if (subscription.getCurrentPeriodStart() == null) {
                subscription.setCurrentPeriodStart(LocalDateTime.now());
            }
            if (subscription.getCurrentPeriodEnd() == null) {
                subscription.setCurrentPeriodEnd(calculatePeriodEnd(subscription.getPlan()));
            }
        }

        subscriptionRepository.save(subscription);

        log.info("Abonnement {} : statut changé de {} à {}", subscriptionId, previousStatus, subscription.getStatus().name());
        log.info("Abonnement {} activé avec Stripe ID: {}", subscriptionId, stripeSubscriptionId);
        
        return ApiResponse.<Void>builder()
                .status(204)
                .success(true)
                .message("Abonnement activé avec succès !")
                .build();
    }
    
    
    private LocalDateTime calculatePeriodEnd(SubscriptionPlan plan) {
        LocalDateTime now = LocalDateTime.now();
        switch (plan.getDurationType()) {
            case "WEEK":
                return now.plusWeeks(plan.getDurationValue());
            case "MONTH":
                return now.plusMonths(plan.getDurationValue());
            case "YEAR":
                return now.plusYears(plan.getDurationValue());
            default:
                return now.plusYears(100);
        }
    }
    
    private SubscriptionStatus mapStripeStatusToOurStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return SubscriptionStatus.PENDING;
        }

        switch (stripeStatus.toLowerCase()) {
            case "active":
                return SubscriptionStatus.ACTIVE;
            case "canceled":
            case "cancelled":
                return SubscriptionStatus.CANCELLED;
            case "past_due":
                return SubscriptionStatus.PAST_DUE;
            case "unpaid":
            case "incomplete":
            case "incomplete_expired":
                return SubscriptionStatus.EXPIRED;
            default:
                log.warn("Statut Stripe inconnu: {}, utilisation de PENDING par défaut", stripeStatus);
                return SubscriptionStatus.PENDING;
        }
    }
}
