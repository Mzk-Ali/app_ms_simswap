package com.simswap.auth_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
