package com.projectmanagement.app.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.projectmanagement.app.user.User;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpEmailVerificationDeliveryService implements EmailVerificationDeliveryService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public SmtpEmailVerificationDeliveryService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void deliver(User user, String rawToken) {
        String verificationUrl = frontendUrl + "/verify-email?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Verify your email address");
        message.setText("Use this link to verify your email. It expires in 24 hours:\n" + verificationUrl);
        mailSender.send(message);
    }
}
