package com.projectmanagement.app.notification;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference,Long>{
 List<NotificationPreference> findByUserIdOrderByEventTypeAsc(Long userId);
 Optional<NotificationPreference> findByUserIdAndEventType(Long userId,String eventType);
}
