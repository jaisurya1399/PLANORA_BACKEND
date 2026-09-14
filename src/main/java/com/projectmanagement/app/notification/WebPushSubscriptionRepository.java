package com.projectmanagement.app.notification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, Long> {
    Optional<WebPushSubscription> findByEndpoint(String endpoint);
    List<WebPushSubscription> findByUserId(Long userId);
    void deleteByUserIdAndEndpoint(Long userId, String endpoint);
}
