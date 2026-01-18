package com.simswap.subscription_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simswap.subscription_service.entities.SubscriptionPlan;


@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByIsActiveTrue();
    
    Optional<SubscriptionPlan> findByCode(String code);
    
    Optional<SubscriptionPlan> findByStripePriceId(String stripePriceId);
    
    Boolean existsByCode(String code);
}
