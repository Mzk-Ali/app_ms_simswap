package com.simswap.subscription_service.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.simswap.subscription_service.dtos.InvoiceResponse;
import com.simswap.subscription_service.entities.UserSubscription;
import com.simswap.subscription_service.repositories.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.param.InvoiceListParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {
	private final UserSubscriptionRepository subscriptionRepository;
	
	public List<InvoiceResponse> getUserInvoices(String userId) {
        UserSubscription subscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId)
                .orElse(null);
        
        if (subscription == null || subscription.getStripeCustomerId() == null) {
            log.warn("Aucun abonnement ou customer Stripe pour userId: {}", userId);
            return new ArrayList<>();
        }
        
        try {
            InvoiceListParams params = InvoiceListParams.builder()
                    .setCustomer(subscription.getStripeCustomerId())
                    .setLimit(50L)
                    .build();
            
            InvoiceCollection invoices = Invoice.list(params);
            
            return invoices.getData().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
                    
        } catch (StripeException e) {
            log.error("Erreur recuperation factures Stripe: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .amount(BigDecimal.valueOf(invoice.getAmountPaid()).divide(BigDecimal.valueOf(100)))
                .currency(invoice.getCurrency().toUpperCase())
                .status(invoice.getStatus())
                .invoiceUrl(invoice.getHostedInvoiceUrl())
                .invoicePdf(invoice.getInvoicePdf())
                .createdAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(invoice.getCreated()),
                        ZoneId.systemDefault()))
                .build();
    }
}
