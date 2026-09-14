package com.projectmanagement.app.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Local fixed-window protection against password guessing. Production deployments
 * running more than one API instance should replace this store with Redis.
 */
@Service
public class LoginRateLimitService {
    private final int maxAttempts;
    private final Duration window;
    private final ConcurrentHashMap<String, AttemptWindow> emailAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptWindow> ipAttempts = new ConcurrentHashMap<>();

    public LoginRateLimitService(
            @Value("${auth.login.max-attempts:5}") int maxAttempts,
            @Value("${auth.login.window-minutes:15}") long windowMinutes) {
        if (maxAttempts < 1 || windowMinutes < 1) {
            throw new IllegalArgumentException("Login rate-limit configuration must be positive");
        }
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public void checkAllowed(String email, String clientIp) {
        long now = Instant.now().toEpochMilli();
        long retryAfterMillis = Math.max(remainingMillis(emailAttempts, email, now),
                remainingMillis(ipAttempts, clientIp, now));
        if (retryAfterMillis > 0) {
            long retryAfterSeconds = Math.max(1, (retryAfterMillis + 999) / 1000);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Try again in " + retryAfterSeconds + " seconds.");
        }
    }

    public void recordFailure(String email, String clientIp) {
        long now = Instant.now().toEpochMilli();
        increment(emailAttempts, email, now);
        increment(ipAttempts, clientIp, now);
    }

    public void recordSuccess(String email, String clientIp) {
        emailAttempts.remove(email);
        ipAttempts.remove(clientIp);
    }

    private long remainingMillis(ConcurrentHashMap<String, AttemptWindow> attempts, String key, long now) {
        AttemptWindow attemptWindow = attempts.get(key);
        if (attemptWindow == null) {
            return 0;
        }
        synchronized (attemptWindow) {
            if (attemptWindow.isExpired(now, window.toMillis())) {
                attempts.remove(key, attemptWindow);
                return 0;
            }
            return attemptWindow.failures >= maxAttempts
                    ? attemptWindow.startedAtMillis + window.toMillis() - now
                    : 0;
        }
    }

    private void increment(ConcurrentHashMap<String, AttemptWindow> attempts, String key, long now) {
        attempts.compute(key, (ignored, attemptWindow) -> {
            if (attemptWindow == null || attemptWindow.isExpired(now, window.toMillis())) {
                return new AttemptWindow(now);
            }
            attemptWindow.failures++;
            return attemptWindow;
        });
    }

    private static final class AttemptWindow {
        private final long startedAtMillis;
        private int failures = 1;

        private AttemptWindow(long startedAtMillis) {
            this.startedAtMillis = startedAtMillis;
        }

        private boolean isExpired(long now, long windowMillis) {
            return now - startedAtMillis >= windowMillis;
        }
    }
}
