package com.simswap.auth_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
	
	@GetMapping("")
	public String index() {
		return "API Auth Service fonctionne";
	}
}
