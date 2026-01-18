package com.simswap.subscription_service.services;

import org.springframework.stereotype.Service;

import com.simswap.subscription_service.enums.PaymentStatus;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookService {
    private final UserSubscriptionService userSubscriptionService;
    private final PaymentService paymentService;
    
    public void handleStripeEvent(Event event) {
        log.info("Traitement webhook Stripe: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
//            case "invoice.paid":
//                handleInvoicePaid(event);
//                break;
//            case "invoice.payment_failed":
//                handleInvoicePaymentFailed(event);
//                break;
            default:
                log.info("Type d'événement non géré: {}", event.getType());
        }
    }
    
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if (session == null) {
                log.error("Impossible de deserializer la session");
                return;
            }

            String subscriptionId = session.getMetadata().get("subscriptionId");
            String stripeSubscriptionId = session.getSubscription();
            String stripeCustomerId = session.getCustomer();

            userSubscriptionService.activateSubscription(
                    subscriptionId, 
                    stripeSubscriptionId, 
                    stripeCustomerId
            );
            
            log.info("Checkout session complétée pour subscription: {}", subscriptionId);

        } catch (Exception e) {
            log.error("Erreur traitement checkout.session.completed: {}", e.getMessage(), e);
        }
    }
    
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

            if (paymentIntent == null) return;

            paymentService.updatePaymentStatus(
                    paymentIntent.getId(), 
                    PaymentStatus.SUCCESS,
                    paymentIntent.getLatestCharge()
            );

            log.info("Paiement réussi: {}", paymentIntent.getId());

        } catch (Exception e) {
            log.error("Erreur traitement payment_intent.succeeded: {}", e.getMessage());
        }
    }
    
    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = 
                (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

            if (paymentIntent == null) return;

            paymentService.updatePaymentStatus(
                    paymentIntent.getId(), 
                    PaymentStatus.FAILED,
                    null
            );

            log.warn("Paiement échoué: {}", paymentIntent.getId());

        } catch (Exception e) {
            log.error("Erreur traitement payment_intent.payment_failed: {}", e.getMessage());
        }
    }
}
