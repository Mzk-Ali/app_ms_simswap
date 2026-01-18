package com.simswap.subscription_service.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionValidationResponse {
    private String userId;
    private Boolean isActive;
    private Boolean canUseService;
    private String subscriptionStatus;
    private Integer limit;
    private Integer used;
    private Integer remaining;
    private LocalDateTime periodEnd;
    private String planCode;
}
