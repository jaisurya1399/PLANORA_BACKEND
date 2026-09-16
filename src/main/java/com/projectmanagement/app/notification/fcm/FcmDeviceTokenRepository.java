package com.projectmanagement.app.notification.fcm;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceToken, Long> {
    List<FcmDeviceToken> findByUserId(Long userId);

    Optional<FcmDeviceToken> findByToken(String token);

    void deleteByToken(String token);
}
