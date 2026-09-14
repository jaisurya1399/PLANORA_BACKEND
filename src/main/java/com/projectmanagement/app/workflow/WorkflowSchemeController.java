package com.projectmanagement.app.workflow;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/workflow-schemes")
@RequiredArgsConstructor
public class WorkflowSchemeController {
    private final WorkflowSchemeService service;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('workflow.view') or hasRole('ADMIN')")
    public List<WorkflowSchemeResponse> list(@PathVariable Long projectId) {
        return service.list(projectId);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('workflow.update') or hasRole('ADMIN')")
    public ResponseEntity<WorkflowSchemeResponse> save(@Valid @RequestBody WorkflowSchemeRequest request) {
        return ResponseEntity.ok(service.save(request));
    }
}
