package com.simswap.subscription_service.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private String code;
    private String durationType;
    private Integer durationValue;
    private BigDecimal price;
    private String currency;
    private Integer simswapLimit;
    private String stripePriceId;
    private Map<String, Object> features;
    private List<String> featuresList;
}
