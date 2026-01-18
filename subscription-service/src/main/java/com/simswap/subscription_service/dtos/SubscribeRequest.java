package com.simswap.subscription_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscribeRequest {
    @NotBlank(message = "L'ID utilisateur est obligatoire")
    private String userId;
    
    @NotNull(message = "Le plan est obligatoire")
    private Long planId;
}
