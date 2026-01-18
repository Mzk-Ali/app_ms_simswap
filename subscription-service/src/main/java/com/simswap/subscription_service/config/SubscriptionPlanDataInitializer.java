package com.simswap.subscription_service.config;

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
                                .isActive(true)
                                .build()
                );
            }
        };
    }
}
