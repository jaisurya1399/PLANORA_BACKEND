package com.projectmanagement.app.automation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.notification.NotificationRequest;
import com.projectmanagement.app.notification.NotificationService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomationEngine {
    private final AutomationRuleRepository rules;
    private final TicketRepository tickets;
    private final NotificationService notifications;
    private final ProjectRepository projects;

    public int process(AutomationEventRequest x) {
        List<AutomationRule> rs = rules.findByEnabledTrueAndTriggerEvent(x.getEventType());
        int count = 0;
        Ticket t = x.getTicketId() == null ? null : tickets.findById(x.getTicketId()).orElse(null);
        for (AutomationRule r : rs) {
            if (r.getProject() != null && !r.getProject().getId().equals(x.getProjectId()))
                continue;
            if (!matches(r.getConditionExpression(), t))
                continue;
            Long recipient = recipient(r.getActionType(), t);
            if (recipient != null) {
                NotificationRequest n = new NotificationRequest();
                n.setType("AUTOMATION");
                n.setNotifiableType("user");
                n.setNotifiableId(recipient);
                n.setData(x.getMessage() == null ? "Automation rule executed: " + r.getName() : x.getMessage());
                notifications.create(n);
            }
            r.setLastRunAt(LocalDateTime.now());
            count++;
        }
        return count;
    }

    private boolean matches(String c, Ticket t) {
        if (c == null || c.isBlank() || "ALWAYS".equalsIgnoreCase(c))
            return true;
        if (t == null)
            return false;
        String[] a = c.split("=", 2);
        if (a.length != 2)
            return false;
        String k = a[0].trim().toUpperCase(), v = a[1].trim();
        if ("PRIORITY".equals(k))
            return t.getPriority() != null && v.equalsIgnoreCase(t.getPriority().getName());
        if ("STATUS".equals(k))
            return t.getStatus() != null && v.equalsIgnoreCase(t.getStatus().getName());
        if ("ASSIGNED".equals(k))
            return t.getResponsible() != null && Boolean.parseBoolean(v) == true;
        return false;
    }

    private Long recipient(String action, Ticket t) {
        if (t == null)
            return null;
        if ("NOTIFY_ASSIGNEE".equalsIgnoreCase(action) && t.getResponsible() != null)
            return t.getResponsible().getId();
        if ("NOTIFY_OWNER".equalsIgnoreCase(action) && t.getOwner() != null)
            return t.getOwner().getId();
        return null;
    }
}
