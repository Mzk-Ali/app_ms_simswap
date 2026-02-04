package com.simswap.subscription_service.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.simswap.subscription_service.entities.SubscriptionPlan;

@DataJpaTest
@ActiveProfiles("test")
public class SubscriptionPlanRepositoryIntegrationTest {
    @Autowired
    private SubscriptionPlanRepository planRepository;
    @BeforeEach
    void setUp() {
        planRepository.deleteAll();
    }
    
    @Test
    void savePlan_ShouldPersistAndRetrievePlan() {
        // Arrange - Préparaton des données : Création d'un plan
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name("Test Plan")
                .code("TEST_PLAN")
                .durationType("monthly")
                .durationValue(1)
                .simswapLimit(10)
                .priceAmount(BigDecimal.valueOf(19.99))
                .currency("EUR")
                .isActive(true)
                .build();

        // Act - Exécution du sauvegarde du plan
        SubscriptionPlan savedPlan = planRepository.save(plan);

        // Assert - Vérification que le plan est bien sauvegardé
        assertNotNull(savedPlan.getId());
        assertEquals("Test Plan", savedPlan.getName());
        assertEquals("TEST_PLAN", savedPlan.getCode());
        assertEquals(10, savedPlan.getSimswapLimit());
        assertTrue(savedPlan.getIsActive());
        assertNotNull(savedPlan.getCreatedAt());
        assertNotNull(savedPlan.getUpdatedAt());
    }
}
