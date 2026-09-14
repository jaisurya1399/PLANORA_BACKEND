package com.projectmanagement.app.securityscheme;

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
@RequestMapping("/api/project-permission-schemes")
@RequiredArgsConstructor
public class ProjectPermissionSchemeController {
    private final ProjectPermissionSchemeService service;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('permission_scheme.view') or hasRole('ADMIN')")
    public ProjectPermissionSchemeResponse get(@PathVariable Long projectId) {
        return service.get(projectId);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('permission_scheme.update') or hasRole('ADMIN')")
    public ProjectPermissionSchemeResponse save(@Valid @RequestBody ProjectPermissionSchemeRequest r) {
        return service.save(r);
    }
}
