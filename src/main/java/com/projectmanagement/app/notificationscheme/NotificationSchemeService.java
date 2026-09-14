package com.projectmanagement.app.notificationscheme;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSchemeService {
    private final NotificationSchemeRepository schemes;
    private final NotificationSchemeRuleRepository rules;
    private final ProjectRepository projects;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public NotificationSchemeResponse get(Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        NotificationScheme s = schemes.findByProjectId(projectId).orElse(null);
        if (s == null)
            return NotificationSchemeResponse.builder().projectId(projectId).name("Default Notification Scheme")
                    .enabled(true).rules(List.of()).build();
        return resp(s);
    }

    public NotificationSchemeResponse save(NotificationSchemeRequest x) {
        Project p = projects.findById(x.getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireManager(p);
        NotificationScheme s = schemes.findByProjectId(p.getId())
                .orElse(NotificationScheme.builder().project(p).build());
        s.setName(x.getName());
        return resp(schemes.save(s));
    }

    public NotificationSchemeResponse addRule(Long schemeId, NotificationSchemeRuleRequest x) {
        NotificationScheme s = schemes.findById(schemeId).orElseThrow(() -> new RuntimeException("Scheme not found"));
        access.requireManager(s.getProject());
        NotificationSchemeRule r = NotificationSchemeRule.builder().scheme(s).eventType(x.getEventType())
                .recipientType(x.getRecipientType()).inAppEnabled(x.getInAppEnabled() == null || x.getInAppEnabled())
                .emailEnabled(x.getEmailEnabled() == null || x.getEmailEnabled()).build();
        rules.save(r);
        return resp(s);
    }

    private NotificationSchemeResponse resp(NotificationScheme s) {
        return NotificationSchemeResponse.builder().id(s.getId()).projectId(s.getProject().getId()).name(s.getName())
                .enabled(s.isEnabled()).rules(rules.findBySchemeId(s.getId())).build();
    }
}
