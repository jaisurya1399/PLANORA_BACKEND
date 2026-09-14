package com.projectmanagement.app.customfield;

import java.util.List;

import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/field-configurations")
@RequiredArgsConstructor
public class FieldConfigurationController {
    private final FieldConfigurationService service;

    @GetMapping
    @PreAuthorize("hasAuthority('field_configuration.view') or hasRole('ADMIN')")
    public List<FieldConfigurationResponse> all() {
        return service.all();
    }

    @GetMapping("/effective/project/{projectId}/ticket-type/{ticketTypeId}")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public List<FieldConfigurationResponse> effective(@PathVariable Long projectId, @PathVariable Long ticketTypeId) {
        return service.effective(projectId, ticketTypeId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('field_configuration.create') or hasRole('ADMIN')")
    public ResponseEntity<FieldConfigurationResponse> create(@Valid @RequestBody FieldConfigurationRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('field_configuration.update') or hasRole('ADMIN')")
    public FieldConfigurationResponse update(@PathVariable Long id, @Valid @RequestBody FieldConfigurationRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('field_configuration.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
