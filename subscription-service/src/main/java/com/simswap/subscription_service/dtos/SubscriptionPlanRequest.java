package com.simswap.subscription_service.dtos;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SubscriptionPlanRequest {
    @NotBlank(message = "Le nom du plan est obligatoire")
    private String name;
    
    @NotBlank(message = "Le code du plan est obligatoire")
    private String code;
    
    @NotBlank(message = "Le type de durée est obligatoire")
    private String durationType; // WEEK, MONTH, YEAR, UNLIMITED
    
    private Integer durationValue;
    
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal priceAmount;
    
    private String currency;
    
    @NotNull(message = "La limite SimSwap est obligatoire")
    @Positive(message = "La limite doit être positive")
    private Integer simswapLimit;
    
    private String stripePriceId;
    
    private Map<String, Object> features;
}
