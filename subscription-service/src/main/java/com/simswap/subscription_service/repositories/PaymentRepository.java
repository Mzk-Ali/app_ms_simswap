package com.simswap.subscription_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simswap.subscription_service.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
