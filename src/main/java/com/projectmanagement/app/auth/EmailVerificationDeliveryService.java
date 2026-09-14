package com.projectmanagement.app.auth;

import com.projectmanagement.app.user.User;

/**
 * Delivers an email-verification token through the configured email provider.
 */
public interface EmailVerificationDeliveryService {
    void deliver(User user, String rawToken);
}
