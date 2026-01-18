package com.simswap.subscription_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionRequest {
    @NotBlank(message = "L'ID utilisateur est obligatoire")
    private String userId;
    
    @NotNull(message = "L'ID du plan est obligatoire")
    private Long planId;
    
    @NotNull(message = "L'ID de l'abonnement est obligatoire")
    private Long subscriptionId;
}
