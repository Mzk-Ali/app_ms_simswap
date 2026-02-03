package com.simswap.subscription_service.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simswap.subscription_service.dtos.ApiResponse;
import com.simswap.subscription_service.dtos.InvoiceResponse;
import com.simswap.subscription_service.services.InvoiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
private final InvoiceService invoiceService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(
            @RequestHeader("X-Auth-User-Id") String userId) {
        
        log.info("Recuperation factures pour userId: {}", userId);
        
        List<InvoiceResponse> invoices = invoiceService.getUserInvoices(userId);
        
        return ResponseEntity.ok(ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .data(invoices)
                .message("Factures recuperees")
                .build());
    }
}
