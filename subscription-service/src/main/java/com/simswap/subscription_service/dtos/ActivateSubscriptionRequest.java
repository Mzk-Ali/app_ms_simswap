package com.simswap.subscription_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateSubscriptionRequest {
    @NotBlank(message = "Le stripeSubscriptionId est obligatoire")
    private String stripeSubscriptionId;
    
    @NotBlank(message = "Le stripeCustomerId est obligatoire")
    private String stripeCustomerId;
}
