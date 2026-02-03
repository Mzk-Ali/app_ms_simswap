package com.simswap.subscription_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
	private String id;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String invoiceUrl;
    private String invoicePdf;
    private LocalDateTime createdAt;
}
