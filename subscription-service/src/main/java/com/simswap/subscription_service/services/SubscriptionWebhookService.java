package com.simswap.subscription_service.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.simswap.subscription_service.entities.UserSubscription;
import com.simswap.subscription_service.enums.SubscriptionStatus;
import com.simswap.subscription_service.repositories.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceLineItem;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionWebhookService {
    private final UserSubscriptionRepository subscriptionRepository;
    
    public void handleStripeEvent(Event event) {
        log.info("Traitement webhook Stripe pour abonnement: {}", event.getType());

        switch (event.getType()) {
            case "customer.subscription.created":
                handleSubscriptionCreated(event);
                break;
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            case "invoice.paid":
                handleInvoicePaid(event);
                break;
            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event);
                break;
            default:
                log.info("Type d'événement non géré: {}", event.getType());
        }
    }
    
    private void handleSubscriptionCreated(Event event) {
        try {
            Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);

            if (stripeSubscription == null) {
                log.error("Impossible de deserializer la subscription");
                return;
            }

            log.info("Abonnement Stripe créé: {}", stripeSubscription.getId());

            String subscriptionId = stripeSubscription.getMetadata().get("subscriptionId");
            
            if (subscriptionId != null) {
                UserSubscription userSubscription = subscriptionRepository
                        .findById(Long.valueOf(subscriptionId))
                        .orElse(null);

                if (userSubscription != null) {
                    updateSubscriptionFromStripe(userSubscription, stripeSubscription);
                    subscriptionRepository.save(userSubscription);
                    log.info("Abonnement {} mis à jour avec Stripe ID: {}", subscriptionId, stripeSubscription.getId());
                }
            }

        } catch (Exception e) {
            log.error("Erreur traitement customer.subscription.created: {}", e.getMessage(), e);
        }
    }
    
    private void handleSubscriptionUpdated(Event event) {
        try {
            Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);

            if (stripeSubscription == null) {
                log.error("Impossible de deserializer la subscription");
                return;
            }

            log.info("Abonnement Stripe mis à jour: {}", stripeSubscription.getId());

            UserSubscription userSubscription = subscriptionRepository
                    .findByStripeSubscriptionId(stripeSubscription.getId())
                    .orElse(null);

            if (userSubscription != null) {
                String previousStatus = userSubscription.getStatus().name();
                
                updateSubscriptionFromStripe(userSubscription, stripeSubscription);
                
                if (!previousStatus.equals(userSubscription.getStatus().name())) {
                    log.info("Changement de statut pour abonnement {}: {} -> {}", userSubscription.getId(), previousStatus, userSubscription.getStatus());
                }

                subscriptionRepository.save(userSubscription);
            } else {
                log.warn("Abonnement non trouvé pour Stripe ID: {}", stripeSubscription.getId());
            }

        } catch (Exception e) {
            log.error("Erreur traitement customer.subscription.updated: {}", e.getMessage(), e);
        }
    }
    
    private void handleSubscriptionDeleted(Event event) {
        try {
            Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);

            if (stripeSubscription == null) {
                log.error("Impossible de deserializer la subscription");
                return;
            }

            log.info("Abonnement Stripe supprimé: {}", stripeSubscription.getId());

            UserSubscription userSubscription = subscriptionRepository
                    .findByStripeSubscriptionId(stripeSubscription.getId())
                    .orElse(null);

            if (userSubscription != null) {
                String previousStatus = userSubscription.getStatus().name();
                
                // Marquer comme annulé
                userSubscription.setStatus(SubscriptionStatus.CANCELLED);
                userSubscription.setCancelledAt(LocalDateTime.now());

                subscriptionRepository.save(userSubscription);

                log.info("Abonnement {} : statut changé de {} à {}", userSubscription.getId(), previousStatus, userSubscription.getStatus().name());
                log.info("Abonnement {} marqué comme annulé", userSubscription.getId());

                // TODO: Envoyer email de confirmation d'annulation
                // emailService.sendCancellationEmail(userSubscription.getUserId());
            }

        } catch (Exception e) {
            log.error("Erreur traitement customer.subscription.deleted: {}", e.getMessage(), e);
        }
    }
    
    
    private void handleInvoicePaid(Event event) {
        try {
            Invoice invoice = 
                (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);

            if (invoice == null) {
                log.error("Impossible de deserializer l'invoice");
                return;
            }

            log.info("Facture payée: {}", invoice.getId());

            String stripeSubscriptionId = getSubscriptionIdFromInvoice(invoice);

            if (stripeSubscriptionId != null) {
                UserSubscription userSubscription = subscriptionRepository
                        .findByStripeSubscriptionId(stripeSubscriptionId)
                        .orElse(null);

                if (userSubscription != null) {
                    String previousStatus = userSubscription.getStatus().name();
                    
                    if (userSubscription.getStatus() == SubscriptionStatus.PAST_DUE) {
                        userSubscription.setStatus(SubscriptionStatus.ACTIVE);
                        
                        subscriptionRepository.save(userSubscription);

                        log.info("Abonnement {} : statut changé de {} à {}", userSubscription, previousStatus, userSubscription.getStatus().name());
                        log.info("Abonnement {} réactivé après paiement réussi", userSubscription.getId());
                    }

                    // TODO: Envoyer email avec la facture
                    // emailService.sendInvoiceEmail(userSubscription.getUserId(), invoice);
                }
            }

        } catch (Exception e) {
            log.error("Erreur traitement invoice.paid: {}", e.getMessage(), e);
        }
    }
    
    private void handleInvoicePaymentFailed(Event event) {
        try {
            Invoice invoice = 
                (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);

            if (invoice == null) {
                log.error("Impossible de deserializer l'invoice");
                return;
            }

            log.warn("Échec du paiement de la facture: {}", invoice.getId());

            String stripeSubscriptionId = getSubscriptionIdFromInvoice(invoice);

            if (stripeSubscriptionId != null) {
                UserSubscription userSubscription = subscriptionRepository
                        .findByStripeSubscriptionId(stripeSubscriptionId)
                        .orElse(null);

                if (userSubscription != null) {
                    String previousStatus = userSubscription.getStatus().name();
                    
                    userSubscription.setStatus(SubscriptionStatus.PAST_DUE);
                    
                    subscriptionRepository.save(userSubscription);

                    log.info("Abonnement {} : statut changé de {} à {}", userSubscription, previousStatus, userSubscription.getStatus().name());
                    log.warn("Abonnement {} marqué comme PAST_DUE", userSubscription.getId());

                    // TODO: Envoyer email d'alerte de paiement échoué
                    // emailService.sendPaymentFailedEmail(userSubscription.getUserId(), invoice);
                }
            }

        } catch (Exception e) {
            log.error("Erreur traitement invoice.payment_failed: {}", e.getMessage(), e);
        }
    }
    
    
    private void updateSubscriptionFromStripe(UserSubscription userSubscription, Subscription stripeSubscription)
    {
        userSubscription.setStripeSubscriptionId(stripeSubscription.getId());
        userSubscription.setStripeCustomerId(stripeSubscription.getCustomer());
        
        if (stripeSubscription.getItems() != null && 
            stripeSubscription.getItems().getData() != null &&
            !stripeSubscription.getItems().getData().isEmpty()) 
        {
        
            SubscriptionItem firstItem = stripeSubscription.getItems().getData().get(0);
            
            if (firstItem.getCurrentPeriodStart() != null) {
                userSubscription.setCurrentPeriodStart(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(firstItem.getCurrentPeriodStart()),
                        ZoneId.systemDefault()
                    )
                );
            }
            
            if (firstItem.getCurrentPeriodEnd() != null) {
                userSubscription.setCurrentPeriodEnd(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(firstItem.getCurrentPeriodEnd()),
                        ZoneId.systemDefault()
                    )
                );
            }
        } else {
            log.warn("Aucun item trouvé dans l'abonnement Stripe: {}", stripeSubscription.getId());
        }
        
        SubscriptionStatus status = mapStripeStatusToOurStatus(stripeSubscription.getStatus());
        userSubscription.setStatus(status);
        
        Boolean cancelAtPeriodEnd = stripeSubscription.getCancelAtPeriodEnd();
        userSubscription.setCancelAtPeriodEnd(cancelAtPeriodEnd != null ? cancelAtPeriodEnd : false);
        
        log.debug("Abonnement {} mis à jour avec statut Stripe: {}", 
        userSubscription.getId(), stripeSubscription.getStatus());
    }
    
    private String getSubscriptionIdFromInvoice(Invoice invoice) {
        if (invoice.getLines() != null && invoice.getLines().getData() != null) {
            for (InvoiceLineItem lineItem : invoice.getLines().getData()) {
                if (lineItem.getSubscription() != null) {
                    return lineItem.getSubscription();
                }
                
                if (lineItem.getMetadata() != null && lineItem.getMetadata().containsKey("subscription_id")) {
                    return lineItem.getMetadata().get("subscription_id");
                }
            }
        }
        
        if (invoice.getMetadata() != null && invoice.getMetadata().containsKey("subscription_id")) {
            return invoice.getMetadata().get("subscription_id");
        }
        try {
            Invoice fullInvoice = Invoice.retrieve(invoice.getId());
            if (fullInvoice.getLines() != null && fullInvoice.getLines().getData() != null) {
                for (InvoiceLineItem lineItem : fullInvoice.getLines().getData()) {
                    if (lineItem.getSubscription() != null) {
                        return lineItem.getSubscription();
                    }
                }
            }
        } catch (StripeException e) {
            log.error("Erreur récupération facture complète: {}", e.getMessage());
        }
        log.warn("Impossible de trouver l'ID d'abonnement pour la facture: {}", invoice.getId());
        return null;
    }
    
    private SubscriptionStatus mapStripeStatusToOurStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return SubscriptionStatus.PENDING;
        }

        switch (stripeStatus.toLowerCase()) {
            case "active":
                return SubscriptionStatus.ACTIVE;
            case "canceled":
            case "cancelled":
                return SubscriptionStatus.CANCELLED;
            case "past_due":
                return SubscriptionStatus.PAST_DUE;
            case "unpaid":
            case "incomplete":
            case "incomplete_expired":
                return SubscriptionStatus.EXPIRED;
            default:
                log.warn("Statut Stripe inconnu: {}, utilisation de PENDING par défaut", stripeStatus);
                return SubscriptionStatus.PENDING;
        }
    }
}
