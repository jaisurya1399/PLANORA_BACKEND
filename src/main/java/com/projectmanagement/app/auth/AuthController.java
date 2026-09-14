package com.projectmanagement.app.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final ClientIpResolver clientIpResolver;
    private final AuthAbuseRateLimitService authAbuseRateLimitService;

    public AuthController(AuthService authService, ClientIpResolver clientIpResolver,
            AuthAbuseRateLimitService authAbuseRateLimitService) {
        this.authService = authService;
        this.clientIpResolver = clientIpResolver;
        this.authAbuseRateLimitService = authAbuseRateLimitService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                authService.login(request, clientIpResolver.resolve(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request,
            HttpServletRequest httpRequest) {
        authAbuseRateLimitService.checkAndRecord("mfa:" + clientIpResolver.resolve(httpRequest));
        return ResponseEntity.ok(authService.verifyMfa(request, clientIpResolver.resolve(httpRequest),
                httpRequest.getHeader("User-Agent")));
    }

    @GetMapping("/mfa/status")
    public ResponseEntity<Boolean> mfaStatus(Authentication authentication) {
        return ResponseEntity.ok(authService.isMfaEnabled(authentication.getName()));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(Authentication authentication) {
        return ResponseEntity.ok(authService.setupMfa(authentication.getName()));
    }

    @PostMapping("/mfa/confirm")
    public ResponseEntity<MfaSetupResponse> confirmMfa(Authentication authentication,
            @Valid @RequestBody MfaConfirmRequest request) {
        return ResponseEntity.ok(authService.confirmMfa(authentication.getName(), request.getCode()));
    }

    @PostMapping("/mfa/disable")
    public ResponseEntity<Void> disableMfa(Authentication authentication,
            @Valid @RequestBody MfaDisableRequest request) {
        authService.disableMfa(authentication.getName(), request.getCode());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization,
            Authentication authentication,
            @Valid @RequestBody LogoutRequest request) {
        if (!authorization.startsWith("Bearer ") || authorization.length() <= 7)
            return ResponseEntity.badRequest().build();
        authService.logout(authorization.substring(7), authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> sessions(Authentication authentication,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        return ResponseEntity.ok(authService.getSessions(authentication.getName(), refreshToken));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> revokeSession(Authentication authentication, @PathVariable Long id) {
        authService.revokeSession(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<Void> revokeAllSessions(Authentication authentication) {
        authService.revokeAllSessions(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        authAbuseRateLimitService.checkAndRecord(
                "reset:" + clientIpResolver.resolve(httpRequest) + ":" + request.getEmail().trim().toLowerCase());
        authService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email-verification/confirm")
    public ResponseEntity<Void> confirmEmailVerification(@Valid @RequestBody EmailVerificationRequest request) {
        authService.confirmEmailVerification(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email-verification/resend")
    public ResponseEntity<Void> resendEmailVerification(Authentication authentication) {
        authService.resendEmailVerification(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request,
            HttpServletRequest httpRequest) {
        authAbuseRateLimitService.checkAndRecord("signup:" + clientIpResolver.resolve(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }
}
