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
@RequestMapping("/api/screen-configurations")
@RequiredArgsConstructor
public class ScreenConfigurationController {
    private final ScreenConfigurationService service;

    @GetMapping
    @PreAuthorize("hasAuthority('screen_configuration.view') or hasRole('ADMIN')")
    public List<ScreenConfigurationResponse> all() {
        return service.all();
    }

    @GetMapping("/effective/project/{projectId}/ticket-type/{ticketTypeId}")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public List<ScreenConfigurationResponse> effective(@PathVariable Long projectId, @PathVariable Long ticketTypeId) {
        return service.effective(projectId, ticketTypeId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('screen_configuration.create') or hasRole('ADMIN')")
    public ResponseEntity<ScreenConfigurationResponse> create(@Valid @RequestBody ScreenConfigurationRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('screen_configuration.update') or hasRole('ADMIN')")
    public ScreenConfigurationResponse update(@PathVariable Long id, @Valid @RequestBody ScreenConfigurationRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('screen_configuration.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/fields")
    @PreAuthorize("hasAuthority('screen_configuration.update') or hasRole('ADMIN')")
    public ScreenConfigurationResponse addField(@PathVariable Long id, @Valid @RequestBody ScreenFieldRequest r) {
        return service.addField(id, r);
    }

    @PutMapping("/{id}/fields/{fieldId}")
    @PreAuthorize("hasAuthority('screen_configuration.update') or hasRole('ADMIN')")
    public ScreenConfigurationResponse updateField(@PathVariable Long id, @PathVariable Long fieldId,
            @RequestBody ScreenFieldRequest r) {
        return service.updateField(id, fieldId, r);
    }

    @DeleteMapping("/{id}/fields/{fieldId}")
    @PreAuthorize("hasAuthority('screen_configuration.update') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeField(@PathVariable Long id, @PathVariable Long fieldId) {
        service.removeField(id, fieldId);
        return ResponseEntity.noContent().build();
    }
}
