package com.simswap.auth_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {
	@NotBlank(message = "Le mot de passe ne doit pas etre vide")
    private String password;
}
