package com.simswap.subscription_service.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
    private int status;
    private boolean success;
    private String message;
    private String code;
    private T data;
    private String path;
    private String traceId;
}
