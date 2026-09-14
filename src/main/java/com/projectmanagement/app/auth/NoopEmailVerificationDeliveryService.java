package com.projectmanagement.app.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.projectmanagement.app.user.User;

/** Safe default: verification tokens are never exposed or written to logs. */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoopEmailVerificationDeliveryService implements EmailVerificationDeliveryService {
    @Override
    public void deliver(User user, String rawToken) {
        // Add a provider-specific implementation before enabling email delivery in
        // production.
    }
}
