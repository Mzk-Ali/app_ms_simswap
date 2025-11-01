package com.simswap.auth_service.dtos;

import lombok.Data;

@Data
public class ChangePasswordRequest {
	private String authUserId;
	private String oldPassword;
	private String newPassword;
	private String confirmNewPassword;
}
