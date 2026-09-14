package com.projectmanagement.app.priorityscheme;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/project-priority-schemes")
@RequiredArgsConstructor
public class ProjectPrioritySchemeController {
    private final ProjectPrioritySchemeService service;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('priority_scheme.view') or hasRole('ADMIN')")
    public ProjectPrioritySchemeResponse get(@PathVariable Long projectId) {
        return service.get(projectId);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('priority_scheme.update') or hasRole('ADMIN')")
    public ProjectPrioritySchemeResponse save(@Valid @RequestBody ProjectPrioritySchemeRequest r) {
        return service.save(r);
    }
}
