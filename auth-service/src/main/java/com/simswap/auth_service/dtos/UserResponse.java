package com.simswap.auth_service.dtos;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String authUserId;
    private String email;
    private Instant createdAt;
    private String role;
}
