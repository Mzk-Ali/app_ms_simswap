package com.simswap.auth_service.dtos;

import lombok.Data;

@Data
public class RequestPasswordResetRequest {
	private String email;
}
