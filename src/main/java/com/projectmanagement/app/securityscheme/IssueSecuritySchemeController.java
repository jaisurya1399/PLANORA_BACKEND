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
@RequestMapping("/api/issue-security-schemes")
@RequiredArgsConstructor
public class IssueSecuritySchemeController {
    private final IssueSecuritySchemeService service;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('issue_security.view') or hasRole('ADMIN')")
    public IssueSecuritySchemeResponse get(@PathVariable Long projectId) {
        return service.get(projectId);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('issue_security.update') or hasRole('ADMIN')")
    public IssueSecuritySchemeResponse save(@Valid @RequestBody IssueSecuritySchemeRequest r) {
        return service.save(r);
    }
}
