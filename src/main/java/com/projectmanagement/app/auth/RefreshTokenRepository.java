package com.projectmanagement.app.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);

    List<RefreshToken> findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(Long userId);

    List<RefreshToken> findByUserId(Long userId);
}
