package com.simswap.subscription_service.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.simswap.subscription_service.entities.SubscriptionPlan;
import com.simswap.subscription_service.repositories.SubscriptionPlanRepository;

@Configuration
public class SubscriptionPlanDataInitializer {
    @Bean
    CommandLineRunner initSubscriptionPlans(SubscriptionPlanRepository repository) {
        return args -> {

            if (!repository.existsByCode("WEEKLY")) {
                repository.save(
                        SubscriptionPlan.builder()
                                .name("Plan Hebdomadaire")
                                .code("WEEKLY")
                                .durationType("WEEK")
                                .durationValue(1)
                                .simswapLimit(5)
                                .priceAmount(BigDecimal.valueOf(1.29))
                                .currency("EUR")
                                .stripePriceId("price_1SwNBr04d4Y2hAFKyVOvM0dm")
                                .isActive(true)
                                .build()
                );
            }

            if (!repository.existsByCode("MONTHLY")) {
                repository.save(
                        SubscriptionPlan.builder()
                                .name("Plan Mensuel")
                                .code("MONTHLY")
                                .durationType("MONTH")
                                .durationValue(1)
                                .simswapLimit(20)
                                .priceAmount(BigDecimal.valueOf(9.99))
                                .currency("EUR")
                                .stripePriceId("price_1SwNCd04d4Y2hAFKqtqOl51W")
                                .isActive(true)
                                .build()
                );
            }

            if (!repository.existsByCode("YEARLY")) {
                repository.save(
                        SubscriptionPlan.builder()
                                .name("Plan Annuel")
                                .code("YEARLY")
                                .durationType("YEAR")
                                .durationValue(1)
                                .simswapLimit(300)
                                .priceAmount(BigDecimal.valueOf(89.99))
                                .currency("EUR")
                                .stripePriceId("price_1SwNCd04d4Y2hAFKqtqOl51W")
                                .isActive(true)
                                .build()
                );
            }
        };
    }
}
