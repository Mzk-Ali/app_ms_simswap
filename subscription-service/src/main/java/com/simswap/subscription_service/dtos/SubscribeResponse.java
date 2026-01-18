package com.simswap.subscription_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeResponse {
    private Long subscriptionId;
    private String checkoutUrl;
    private String sessionId;
}
