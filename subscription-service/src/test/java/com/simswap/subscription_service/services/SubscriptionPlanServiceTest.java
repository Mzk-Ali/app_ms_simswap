package com.simswap.subscription_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.dtos.SubscriptionPlanResponse;
import com.simswap.subscription_service.entities.SubscriptionPlan;
import com.simswap.subscription_service.repositories.SubscriptionPlanRepository;

@ExtendWith(MockitoExtension.class)
public class SubscriptionPlanServiceTest {
    @Mock
    private SubscriptionPlanRepository planRepository;
    @InjectMocks
    private SubscriptionPlanService subscriptionPlanService;
    
    @Test
    void getAllActivePlans_ShouldReturnListOfActivePlans() {
        // Arrange - Préparation des données de test
        SubscriptionPlan plan1 = SubscriptionPlan.builder()
                .id(1L)
                .name("Basic")
                .code("BASIC")
                .simswapLimit(5)
                .isActive(true)
                .build();

        SubscriptionPlan plan2 = SubscriptionPlan.builder()
                .id(2L)
                .name("Premium")
                .code("PREMIUM")
                .simswapLimit(20)
                .isActive(true)
                .build();

        when(planRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(plan1, plan2));

        // Act - Exécution de la méthode à tester
        ApiResponse<List<SubscriptionPlanResponse>> result = subscriptionPlanService.getAllActivePlans();

        // Assert - Vérification des résultats
        assertNotNull(result);
        assertEquals(200, result.getStatus());
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals("Basic", result.getData().get(0).getName());
        assertEquals("PREMIUM", result.getData().get(1).getCode());
        verify(planRepository, times(1)).findByIsActiveTrue();
    }
}
