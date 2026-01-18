package com.simswap.subscription_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.services.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Payment", description = "Endpoints pour la gestion des paiements")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    
    @GetMapping("/{paymentId}/invoice-url")
    public ResponseEntity<ApiResponse<String>> getInvoiceUrl(@PathVariable String paymentId) {
        ApiResponse<String> response = paymentService.getInvoiceUrl(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
}
