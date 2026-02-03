package com.simswap.subscription_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simswap.subscription_service.entities.UserSubscription;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findActiveSubscriptionByUserId(String email);
    
    Optional<UserSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(String userId);
}
