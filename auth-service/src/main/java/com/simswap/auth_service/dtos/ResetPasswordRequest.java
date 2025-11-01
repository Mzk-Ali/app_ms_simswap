package com.simswap.auth_service.dtos;

import lombok.Data;

@Data
public class ResetPasswordRequest {
	private String resetToken;
	private String newPassword;
	private String confirmNewPassword;
}
