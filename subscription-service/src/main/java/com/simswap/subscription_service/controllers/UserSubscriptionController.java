package com.simswap.subscription_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.subscription_service.dtos.ActivateSubscriptionRequest;
import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.dtos.SubscribeRequest;
import com.simswap.subscription_service.dtos.SubscribeResponse;
import com.simswap.subscription_service.dtos.SubscriptionValidationResponse;
import com.simswap.subscription_service.dtos.UserSubscriptionResponse;
import com.simswap.subscription_service.services.UserSubscriptionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Subscription", description = "Endpoints pour la gestion des abonnements")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionController {
    private final UserSubscriptionService subscriptionService;
    
    @GetMapping("")
    public String index() {
        return "API Subscription Service fonctionne";
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getUserSubscription(@PathVariable String userId)
    {
        ApiResponse<UserSubscriptionResponse> response = subscriptionService.getActiveSubscription(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<SubscribeResponse>> subscribe(@Valid @RequestBody SubscribeRequest request)
    {
        log.info("Test");
        ApiResponse<SubscribeResponse> response = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> cancelSubscription(@PathVariable Long subscriptionId)
    {
        ApiResponse<UserSubscriptionResponse> response = subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping("/validate/{userId}")
    public ResponseEntity<ApiResponse<SubscriptionValidationResponse>> validateSubscription(@PathVariable String userId)
    {
        ApiResponse<SubscriptionValidationResponse> response = subscriptionService.validateSubscription(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PutMapping("/{subscriptionId}/sync")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> syncWithStripe(@PathVariable String subscriptionId)
    {
        ApiResponse<UserSubscriptionResponse> response = subscriptionService.syncWithStripe(subscriptionId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/{subscriptionId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateSubscription(@Valid @PathVariable String subscriptionId, @RequestBody ActivateSubscriptionRequest request)
    {
        ApiResponse<Void> response = subscriptionService.activateSubscription(subscriptionId, request.getStripeSubscriptionId(), request.getStripeCustomerId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
