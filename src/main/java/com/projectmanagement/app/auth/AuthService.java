package com.projectmanagement.app.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.projectmanagement.app.project.ProjectMembershipResponse;
import com.projectmanagement.app.project.ProjectUser;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;
import com.projectmanagement.app.userrole.UserRoleRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetDeliveryService passwordResetDeliveryService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailVerificationDeliveryService emailVerificationDeliveryService;
    private final LoginRateLimitService loginRateLimitService;
    private final UserRoleRepository userRoleRepository;
    private final ProjectUserRepository projectUserRepository;
    private final TotpService totpService;
    private final PasswordPolicy passwordPolicy;

    @Value("${auth.refresh-expiration-days:30}")
    private long refreshExpirationDays;
    @Value("${auth.password-reset-expiration-minutes:30}")
    private long passwordResetExpirationMinutes;
    @Value("${auth.email-verification-expiration-hours:24}")
    private long emailVerificationExpirationHours;
    @Value("${app.name:Project Management}")
    private String appName;
    @Value("${auth.signup.enabled:false}")
    private boolean signupEnabled;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
            JwtService jwtService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository,
            RevokedAccessTokenRepository revokedAccessTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetDeliveryService passwordResetDeliveryService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailVerificationDeliveryService emailVerificationDeliveryService,
            LoginRateLimitService loginRateLimitService, UserRoleRepository userRoleRepository,
            ProjectUserRepository projectUserRepository, TotpService totpService, PasswordPolicy passwordPolicy) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetDeliveryService = passwordResetDeliveryService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailVerificationDeliveryService = emailVerificationDeliveryService;
        this.loginRateLimitService = loginRateLimitService;
        this.userRoleRepository = userRoleRepository;
        this.projectUserRepository = projectUserRepository;
        this.totpService = totpService;
        this.passwordPolicy = passwordPolicy;
    }

    public Object login(AuthRequest request, String clientIp, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();
        loginRateLimitService.checkAllowed(email, clientIp);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException exception) {
            loginRateLimitService.recordFailure(email, clientIp);
            throw exception;
        }
        loginRateLimitService.recordSuccess(email, clientIp);

        User user = userRepository.findByEmail(email)
                .filter(existingUser -> existingUser.getDeletedAt() == null)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getTwoFactorConfirmedAt() != null && user.getTwoFactorSecret() != null) {
            return MfaRequiredResponse.builder()
                    .mfaRequired(true)
                    .mfaToken(jwtService.generateMfaChallenge(email))
                    .email(email)
                    .build();
        }
        return issueTokens(user, clientIp, userAgent);
    }

    @Transactional
    public AuthResponse verifyMfa(MfaVerifyRequest request, String clientIp, String userAgent) {
        if (!jwtService.isMfaChallenge(request.getMfaToken()))
            throw new RuntimeException("Invalid or expired MFA challenge");
        String email = jwtService.extractUsername(request.getMfaToken());
        User user = userRepository.findByEmail(email)
                .filter(existingUser -> existingUser.getDeletedAt() == null)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getTwoFactorSecret() == null || user.getTwoFactorConfirmedAt() == null)
            throw new RuntimeException("MFA is not enabled");
        if (!verifyMfaCode(user, request.getCode()))
            throw new RuntimeException("Invalid MFA code");
        return issueTokens(user, clientIp, userAgent);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(request.getRefreshToken()))
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (stored.getRevokedAt() != null || !stored.getExpiresAt().isAfter(LocalDateTime.now())
                || stored.getUser().getDeletedAt() != null)
            throw new RuntimeException("Invalid refresh token");
        stored.setRevokedAt(LocalDateTime.now());
        stored.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);
        User user = stored.getUser();
        return issueTokens(user, stored.getIpAddress(), stored.getUserAgent());
    }

    @Transactional
    public void logout(String accessToken, String authenticatedEmail, LogoutRequest request) {
        refreshTokenRepository.findByTokenHash(hash(request.getRefreshToken())).ifPresent(token -> {
            if (token.getUser().getEmail().equalsIgnoreCase(authenticatedEmail)) {
                token.setRevokedAt(LocalDateTime.now());
                token.setLastUsedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
        String tokenId = jwtService.extractTokenId(accessToken);
        if (tokenId == null || tokenId.isBlank())
            throw new RuntimeException("Invalid access token");
        revokedAccessTokenRepository.save(RevokedAccessToken.builder().tokenId(tokenId)
                .expiresAt(jwtService.extractExpiration(accessToken).toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .build());
    }

    @Transactional
    public List<SessionResponse> getSessions(String email, String currentRefreshToken) {
        User user = getUser(email);
        String currentHash = currentRefreshToken == null ? null : hash(currentRefreshToken);
        return refreshTokenRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(user.getId()).stream()
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(t -> SessionResponse.builder().id(t.getId()).ipAddress(t.getIpAddress())
                        .userAgent(t.getUserAgent()).createdAt(t.getCreatedAt()).lastUsedAt(t.getLastUsedAt())
                        .expiresAt(t.getExpiresAt()).current(t.getTokenHash().equals(currentHash)).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeSession(String email, Long id) {
        RefreshToken token = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!token.getUser().getEmail().equalsIgnoreCase(email))
            throw new RuntimeException("Session not found");
        token.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllSessions(String email) {
        User user = getUser(email);
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(user.getId()).forEach(t -> {
            t.setRevokedAt(now);
            t.setLastUsedAt(now);
        });
    }

    @Transactional
    public MfaSetupResponse setupMfa(String email) {
        User user = getUser(email);
        if (user.getTwoFactorConfirmedAt() != null) {
            return MfaSetupResponse.builder().enabled(true).build();
        }
        String secret = totpService.generateSecret();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorConfirmedAt(null);
        userRepository.save(user);
        return MfaSetupResponse.builder().enabled(false).secret(secret)
                .otpauthUrl(totpService.buildOtpAuthUrl(secret, user.getEmail(), appName)).build();
    }

    @Transactional
    public MfaSetupResponse confirmMfa(String email, String code) {
        User user = getUser(email);
        if (user.getTwoFactorSecret() == null)
            throw new RuntimeException("Start MFA setup first");
        if (!totpService.verify(user.getTwoFactorSecret(), code))
            throw new RuntimeException("Invalid MFA code");
        List<String> recoveryCodes = new ArrayList<>();
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String recovery = randomRecoveryCode();
            recoveryCodes.add(recovery);
            hashes.add(totpService.hashRecoveryCode(recovery));
        }
        user.setTwoFactorRecoveryCodes(String.join("\n", hashes));
        user.setTwoFactorConfirmedAt(LocalDateTime.now());
        userRepository.save(user);
        return MfaSetupResponse.builder().enabled(true).recoveryCodes(recoveryCodes).build();
    }

    @Transactional
    public void disableMfa(String email, String code) {
        User user = getUser(email);
        if (user.getTwoFactorConfirmedAt() == null)
            throw new RuntimeException("MFA is not enabled");
        if (!verifyMfaCode(user, code))
            throw new RuntimeException("Invalid MFA code");
        user.setTwoFactorSecret(null);
        user.setTwoFactorRecoveryCodes(null);
        user.setTwoFactorConfirmedAt(null);
        userRepository.save(user);
    }

    public boolean isMfaEnabled(String email) {
        User user = getUser(email);
        return user.getTwoFactorConfirmedAt() != null && user.getTwoFactorSecret() != null;
    }

    private boolean verifyMfaCode(User user, String code) {
        if (totpService.verify(user.getTwoFactorSecret(), code))
            return true;
        if (user.getTwoFactorRecoveryCodes() == null || code == null)
            return false;
        String target = totpService.hashRecoveryCode(code.trim());
        List<String> remaining = new ArrayList<>();
        boolean matched = false;
        for (String stored : user.getTwoFactorRecoveryCodes().split("\\R")) {
            if (stored.equals(target) && !matched)
                matched = true;
            else if (!stored.isBlank())
                remaining.add(stored);
        }
        if (matched) {
            user.setTwoFactorRecoveryCodes(String.join("\n", remaining));
            userRepository.save(user);
        }
        return matched;
    }

    private AuthResponse issueTokens(User user, String ip, String userAgent) {
        String token = jwtService.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user, ip, userAgent);
        return AuthResponse.builder().accessToken(token).refreshToken(refreshToken).tokenType("Bearer")
                .userId(user.getId()).name(user.getName()).email(user.getEmail())
                .role(getRole(user.getId())).permissions(getPermissions(user.getId()))
                .projectMemberships(getProjectMemberships(user.getId())).mfaRequired(false).build();
    }

    private String getRole(Long userId) {
        return userRepository.findRoleNameByUserId(userId).map(String::toUpperCase).orElse(null);
    }

    private List<String> getPermissions(Long userId) {
        return userRoleRepository.findUserRolesWithPermissions(userId).stream()
                .filter(r -> r != null && r.getRole() != null)
                .flatMap(r -> r.getRole().getRolePermissions() == null ? java.util.stream.Stream.empty()
                        : r.getRole().getRolePermissions().stream())
                .filter(rp -> rp != null && rp.getPermission() != null && rp.getPermission().getName() != null)
                .map(rp -> rp.getPermission().getName().trim()).filter(s -> !s.isBlank()).distinct().sorted()
                .collect(Collectors.toList());
    }

    private List<ProjectMembershipResponse> getProjectMemberships(Long userId) {
        return projectUserRepository.findByUserId(userId).stream().map(this::toMembership).toList();
    }

    private ProjectMembershipResponse toMembership(ProjectUser membership) {
        return ProjectMembershipResponse.builder()
                .projectId(membership.getProject() != null ? membership.getProject().getId() : null)
                .projectName(membership.getProject() != null ? membership.getProject().getName() : null)
                .role(membership.getRole())
                .responsibilityRole(membership.getResponsibilityRole())
                .build();
    }

    private String createRefreshToken(User user, String ip, String userAgent) {
        String token = randomToken();
        refreshTokenRepository.save(RefreshToken.builder().user(user).tokenHash(hash(token))
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays)).ipAddress(ip)
                .userAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 2000)))
                .lastUsedAt(LocalDateTime.now()).build());
        return token;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private String randomRecoveryCode() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 10).toUpperCase();
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public AuthMeResponse getCurrentUser(String email) {
        User user = getUser(email);
        return AuthMeResponse.builder().userId(user.getId()).name(user.getName()).email(user.getEmail())
                .role(getRole(user.getId())).permissions(getPermissions(user.getId()))
                .projectMemberships(getProjectMemberships(user.getId()))
                .mfaEnabled(isMfaEnabled(user.getEmail())).build();
    }

    /**
     * Starts the password-reset flow for a registered user.
     * The controller intentionally returns 204 for both existing and
     * non-existing addresses to avoid account enumeration.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail() == null
                ? ""
                : request.getEmail().trim().toLowerCase();

        if (email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email)
                .filter(user -> user.getDeletedAt() == null)
                .ifPresent(user -> {
                    LocalDateTime now = LocalDateTime.now();

                    // Invalidate previous unused reset links.
                    passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(user.getId())
                            .forEach(token -> token.setUsedAt(now));

                    String rawToken = randomToken();

                    passwordResetTokenRepository.save(
                            PasswordResetToken.builder()
                                    .user(user)
                                    .tokenHash(hash(rawToken))
                                    .expiresAt(now.plusMinutes(passwordResetExpirationMinutes))
                                    .build());

                    // Never log the raw token.
                    passwordResetDeliveryService.deliver(user, rawToken);
                });
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new RuntimeException("Invalid or expired password reset token");
        }

        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(hash(request.getToken().trim()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(LocalDateTime.now())
                || token.getUser().getDeletedAt() != null)
            throw new RuntimeException("Invalid or expired password reset token");
        User user = token.getUser();
        passwordPolicy.validate(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        token.setUsedAt(LocalDateTime.now());
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(user.getId())
                .forEach(t -> t.setRevokedAt(LocalDateTime.now()));
    }

    @Transactional
    public void confirmEmailVerification(EmailVerificationRequest request) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hash(request.getToken()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired email verification token"));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(LocalDateTime.now())
                || token.getUser().getDeletedAt() != null)
            throw new RuntimeException("Invalid or expired email verification token");
        token.setUsedAt(LocalDateTime.now());
        token.getUser().setEmailVerifiedAt(LocalDateTime.now());
    }

    @Transactional
    public void resendEmailVerification(String email) {
        User user = getUser(email);
        if (user.getEmailVerifiedAt() == null)
            issueEmailVerification(user);
    }

    private void issueEmailVerification(User user) {
        LocalDateTime now = LocalDateTime.now();
        emailVerificationTokenRepository.findByUserIdAndUsedAtIsNull(user.getId())
                .forEach(token -> token.setUsedAt(now));
        String rawToken = randomToken();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder().user(user).tokenHash(hash(rawToken))
                .expiresAt(now.plusHours(emailVerificationExpirationHours)).build());
        emailVerificationDeliveryService.deliver(user, rawToken);
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (!signupEnabled) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resource not found");
        }
        passwordPolicy.validate(request.getPassword());
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent())
            throw new RuntimeException("User already exists with email: " + email);
        User user = User.builder().name(request.getName().trim()).email(email)
                .password(passwordEncoder.encode(request.getPassword())).build();
        User savedUser = userRepository.save(user);
        issueEmailVerification(savedUser);
        return SignupResponse.builder().userId(savedUser.getId()).name(savedUser.getName()).email(savedUser.getEmail())
                .message("User registered successfully. Please verify your email.").build();
    }
}
