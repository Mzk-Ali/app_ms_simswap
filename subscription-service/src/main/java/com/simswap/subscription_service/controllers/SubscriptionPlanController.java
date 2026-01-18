package com.simswap.subscription_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.dtos.SubscriptionPlanResponse;
import com.simswap.subscription_service.services.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/subscriptions/plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {
    private final SubscriptionPlanService planService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllPlans() {
        ApiResponse<List<SubscriptionPlanResponse>> response = planService.getAllActivePlans();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(@PathVariable Long planId) {
        ApiResponse<SubscriptionPlanResponse> response = planService.getPlanById(planId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
