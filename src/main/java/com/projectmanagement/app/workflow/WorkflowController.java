package com.projectmanagement.app.workflow;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService service;

    @GetMapping
    @PreAuthorize("hasAuthority('workflow.view') or hasRole('ADMIN')")
    public List<WorkflowRuleResponse> all() {
        return service.all();
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('workflow.view') or hasRole('ADMIN')")
    public List<WorkflowRuleResponse> byProject(@PathVariable Long projectId) {
        return service.byProject(projectId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('workflow.create') or hasRole('ADMIN')")
    public ResponseEntity<WorkflowRuleResponse> create(@Valid @RequestBody WorkflowRuleRequest r) {
        return ResponseEntity.status(201).body(service.create(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow.update') or hasRole('ADMIN')")
    public WorkflowRuleResponse update(@PathVariable Long id, @Valid @RequestBody WorkflowRuleRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
