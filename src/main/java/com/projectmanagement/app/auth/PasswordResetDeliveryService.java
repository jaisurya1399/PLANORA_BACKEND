package com.projectmanagement.app.auth;

import com.projectmanagement.app.user.User;

/**
 * Delivers a reset token through the application's configured email provider.
 * Implement this adapter with the chosen mail/queue provider; tokens must never
 * be logged.
 */
public interface PasswordResetDeliveryService {
    void deliver(User user, String rawToken);
}
