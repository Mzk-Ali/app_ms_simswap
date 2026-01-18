package com.simswap.subscription_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.dtos.SubscriptionPlanResponse;
import com.simswap.subscription_service.entities.SubscriptionPlan;
import com.simswap.subscription_service.repositories.SubscriptionPlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository planRepository;

    
    public ApiResponse<List<SubscriptionPlanResponse>> getAllActivePlans() {
        List<SubscriptionPlanResponse> response = planRepository.findByIsActiveTrue()
                .stream()
                .<SubscriptionPlanResponse>map(plan -> SubscriptionPlanResponse.builder()
                        .id(plan.getId())
                        .name(plan.getName())
                        .code(plan.getCode())
                        .simswapLimit(plan.getSimswapLimit())
                        .build())
                .toList();
        
        return ApiResponse.<List<SubscriptionPlanResponse>>builder()
                .status(200)
                .success(true)
                .message("")
                .data(response)
                .build();
    }
    
    public ApiResponse<SubscriptionPlanResponse> getPlanById(Long planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan non trouvé pour l'id : " + planId));

        SubscriptionPlanResponse response = SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .code(plan.getCode())
                .simswapLimit(plan.getSimswapLimit())
                .build();
        
        return ApiResponse.<SubscriptionPlanResponse>builder()
                .status(200)
                .success(true)
                .message("")
                .data(response)
                .build();
    }
}
