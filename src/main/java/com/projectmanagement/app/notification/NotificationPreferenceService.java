package com.projectmanagement.app.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPreferenceService {
  public static final List<String> EVENT_TYPES = List.of("TICKET_COMMENT", "TICKET_MENTION", "TICKET_STATUS_CHANGED",
      "TICKET_ASSIGNED", "TICKET_REMINDER", "TICKET_OVERDUE", "DAILY_SCRUM");
  private final NotificationPreferenceRepository repository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<NotificationPreferenceResponse> get(Long userId) {
    User u = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    Map<String, NotificationPreference> existing = new HashMap<>();
    repository.findByUserIdOrderByEventTypeAsc(userId).forEach(p -> existing.put(p.getEventType(), p));
    return EVENT_TYPES.stream().map(type -> {
      NotificationPreference p = existing.get(type);
      return p == null
          ? NotificationPreferenceResponse.builder().userId(u.getId()).eventType(type).inAppEnabled(true)
              .emailEnabled(true).build()
          : toResponse(p);
    }).toList();
  }

  public NotificationPreferenceResponse save(Long userId, NotificationPreferenceRequest request) {
    User u = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    if (!EVENT_TYPES.contains(request.getEventType()))
      throw new IllegalArgumentException("Unsupported notification event: " + request.getEventType());
    NotificationPreference p = repository.findByUserIdAndEventType(userId, request.getEventType())
        .orElseGet(NotificationPreference::new);
    p.setUser(u);
    p.setEventType(request.getEventType());
    p.setInAppEnabled(request.isInAppEnabled());
    p.setEmailEnabled(request.isEmailEnabled());
    return toResponse(repository.save(p));
  }

  @Transactional(readOnly = true)
  public boolean isInAppEnabled(Long userId, String event) {
    return repository.findByUserIdAndEventType(userId, event).map(NotificationPreference::isInAppEnabled).orElse(true);
  }

  @Transactional(readOnly = true)
  public boolean isEmailEnabled(Long userId, String event) {
    return repository.findByUserIdAndEventType(userId, event).map(NotificationPreference::isEmailEnabled).orElse(true);
  }

  private NotificationPreferenceResponse toResponse(NotificationPreference p) {
    return NotificationPreferenceResponse.builder().id(p.getId()).userId(p.getUser().getId())
        .eventType(p.getEventType()).inAppEnabled(p.isInAppEnabled()).emailEnabled(p.isEmailEnabled()).build();
  }
}
