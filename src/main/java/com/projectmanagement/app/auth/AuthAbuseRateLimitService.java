package com.projectmanagement.app.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rate limits public authentication-adjacent endpoints. For multi-instance
 * deployments
 * use a shared store such as Redis instead of the JVM-local map.
 */
@Service
public class AuthAbuseRateLimitService {
    private final int maxAttempts;
    private final Duration window;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public AuthAbuseRateLimitService(
            @Value("${auth.public.max-attempts:5}") int maxAttempts,
            @Value("${auth.public.window-minutes:15}") long windowMinutes) {
        if (maxAttempts < 1 || windowMinutes < 1) {
            throw new IllegalArgumentException("Public auth rate-limit configuration must be positive");
        }
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public void checkAndRecord(String key) {
        long now = Instant.now().toEpochMilli();
        attempts.compute(key, (k, current) -> {
            if (current == null || now - current.startedAt >= window.toMillis()) {
                return new Attempt(now, 1);
            }
            if (current.count >= maxAttempts) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many requests. Please try again later.");
            }
            current.count++;
            return current;
        });
    }

    private static final class Attempt {
        private final long startedAt;
        private int count;

        private Attempt(long startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
