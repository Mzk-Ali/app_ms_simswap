package com.simswap.email_service.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.simswap.email_service.dtos.EmailRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${application.mail.sent.from}")
    private String fromUsr;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setFrom(fromUsr);
        helper.setSubject(subject);
        helper.setText(body, true);
        emailSender.send(message);
    }

    @RabbitListener(queues = "${rabbitmq.queue.email.name}")
    public void processEmailMessage(EmailRequest emailRequest) {
        try {
            log.info("Traitement de l'email pour: {} ", emailRequest.getTo());
            String to = emailRequest.getTo();
            String subject = emailRequest.getSubject();
            String body = generateEmailBody(emailRequest);
            sendEmail(to, subject, body);
            log.info("Email envoyé avec succès à {}", emailRequest.getTo());
        } catch (Exception e) {
            log.error("Erreur lors du traitement du message RabbitMQ: {}", e.getMessage());
        }
    }

    public String generateEmailBody(EmailRequest emailRequest) {
        Context context = new Context();

        if (emailRequest.getDynamicValue() != null) {
        	log.info("Variables dynamiques reçues: {}", emailRequest.getDynamicValue());
            emailRequest.getDynamicValue().forEach(context::setVariable);
        }

        return templateEngine.process("emails/" + emailRequest.getTemplateName(), context);
    }

//    public String loadEmailTemplate(String templateName) {
//        ClassPathResource resource = new ClassPathResource("templates/emails/" + templateName + ".html");
//        try {
//            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            throw new RuntimeException("Error loading email template " + templateName, e);
//        }
//    }
}

