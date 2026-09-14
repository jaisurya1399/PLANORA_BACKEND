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
@RequestMapping("/api/custom-fields")
@RequiredArgsConstructor
public class CustomFieldController {
    private final CustomFieldService service;

    @GetMapping
    @PreAuthorize("hasAuthority('custom_field.view') or hasRole('ADMIN')")
    public List<CustomFieldResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('custom_field.view') or hasRole('ADMIN')")
    public List<CustomFieldResponse> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('custom_field.view') or hasRole('ADMIN')")
    public CustomFieldResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('custom_field.create') or hasRole('ADMIN')")
    public ResponseEntity<CustomFieldResponse> create(@Valid @RequestBody CustomFieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('custom_field.update') or hasRole('ADMIN')")
    public CustomFieldResponse update(@PathVariable Long id, @Valid @RequestBody CustomFieldRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('custom_field.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
