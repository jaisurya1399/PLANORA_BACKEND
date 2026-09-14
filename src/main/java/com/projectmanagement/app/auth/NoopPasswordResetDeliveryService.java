package com.projectmanagement.app.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.projectmanagement.app.user.User;

/**
 * Safe default for deployments without an email integration. It deliberately
 * does not
 * expose or log reset tokens.
 */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoopPasswordResetDeliveryService implements PasswordResetDeliveryService {
    @Override
    public void deliver(User user, String rawToken) {
        // Connect an SMTP/provider implementation before enabling password resets in
        // production.
    }
}
