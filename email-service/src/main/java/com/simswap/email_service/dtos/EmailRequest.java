package com.simswap.email_service.dtos;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> dynamicValue;
}