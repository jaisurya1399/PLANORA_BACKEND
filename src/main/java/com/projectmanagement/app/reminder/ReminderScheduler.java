package com.projectmanagement.app.reminder;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private final ReminderService service;

    @Scheduled(fixedDelayString = "${app.scheduler.reminder.fixed-delay-ms:60000}")
    public void dispatch() {
        service.dispatchDueReminders();
    }
}
