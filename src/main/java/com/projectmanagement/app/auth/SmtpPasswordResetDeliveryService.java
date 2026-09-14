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
public class SmtpPasswordResetDeliveryService implements PasswordResetDeliveryService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;
    private final long expirationMinutes;

    public SmtpPasswordResetDeliveryService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:${spring.mail.username:no-reply@localhost}}") String from,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
            @Value("${auth.password-reset-expiration-minutes:10}") long expirationMinutes) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public void deliver(User user, String rawToken) {
        String resetUrl = frontendUrl + "/reset-password?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Reset your password");
        message.setText("Use this link to reset your Planora password. It expires in " + expirationMinutes + " minutes:\n\n" + resetUrl + "\n\nIf you did not request this, you can safely ignore this email.");
        mailSender.send(message);
    }
}
