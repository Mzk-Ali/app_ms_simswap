package com.simswap.auth_service.publishers;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.simswap.auth_service.dtos.EmailRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.email.name}")
    private String exchange;

    @Value("${rabbitmq.binding.email.name}")
    private String routingKey;
    
    public void sendRegistrationEmail(String email, String token, String verificationUrl) {
        Map<String, Object> model = Map.of(
            "userName", email,
            "token", token,
            "link", verificationUrl
        );
        
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Vérification de compte")
                .templateName("verify-account")
                .dynamicValue(model)
                .build();
        
        log.info("Publication du message RabbitMQ pour l'email : {}", email);
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
    }
}
