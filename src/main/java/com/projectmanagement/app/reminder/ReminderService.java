package com.projectmanagement.app.reminder;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.notification.NotificationRequest;
import com.projectmanagement.app.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReminderService {
    private final ReminderRepository repository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ReminderResponse> mine() {
        return repository.findByUserIdOrderByRemindAtAsc(currentUserService.getCurrentUserId())
                .stream().map(this::toResponse).toList();
    }

    public ReminderResponse create(ReminderRequest request) {
        Reminder r = Reminder.builder()
                .user(currentUserService.getCurrentUser())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .remindAt(request.getRemindAt())
                .build();
        return toResponse(repository.save(r));
    }

    public ReminderResponse cancel(Long id) {
        Reminder r = owned(id);
        r.setStatus("CANCELLED");
        return toResponse(repository.save(r));
    }

    public ReminderResponse snooze(Long id, LocalDateTime remindAt) {
        Reminder r = owned(id);
        if (remindAt == null || !remindAt.isAfter(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Snooze time must be in the future");
        r.setRemindAt(remindAt);
        r.setTriggeredAt(null);
        r.setStatus("PENDING");
        return toResponse(repository.save(r));
    }

    public int dispatchDueReminders() {
        int count = 0;
        for (Reminder r : repository.findTop200ByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc("PENDING",
                LocalDateTime.now())) {
            r.setStatus("TRIGGERED");
            r.setTriggeredAt(LocalDateTime.now());
            repository.save(r);
            NotificationRequest n = new NotificationRequest();
            n.setType("REMINDER");
            n.setNotifiableType("user");
            n.setNotifiableId(r.getUser().getId());
            n.setData(r.getTitle()
                    + (r.getDescription() == null || r.getDescription().isBlank() ? "" : " — " + r.getDescription()));
            notificationService.create(n);
            count++;
        }
        return count;
    }

    private Reminder owned(Long id) {
        return repository.findByIdAndUserId(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
    }

    private ReminderResponse toResponse(Reminder r) {
        return ReminderResponse.builder().id(r.getId()).title(r.getTitle()).description(r.getDescription())
                .remindAt(r.getRemindAt()).status(r.getStatus()).triggeredAt(r.getTriggeredAt())
                .createdAt(r.getCreatedAt()).build();
    }
}
