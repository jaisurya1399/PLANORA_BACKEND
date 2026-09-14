package com.projectmanagement.app.notificationscheme;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notification-schemes")
@RequiredArgsConstructor
public class NotificationSchemeController {
    private final NotificationSchemeService s;

    @GetMapping("/project/{projectId}")
    public NotificationSchemeResponse get(@PathVariable Long projectId) {
        return s.get(projectId);
    }

    @PostMapping
    public NotificationSchemeResponse save(@RequestBody NotificationSchemeRequest r) {
        return s.save(r);
    }

    @PostMapping("/{id}/rules")
    public NotificationSchemeResponse rule(@PathVariable Long id, @RequestBody NotificationSchemeRuleRequest r) {
        return s.addRule(id, r);
    }
}
