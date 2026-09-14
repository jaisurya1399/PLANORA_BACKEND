package com.projectmanagement.app.activity;

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
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService service;

    @GetMapping
    @PreAuthorize("hasAuthority('activity.view') or hasRole('ADMIN')")
    public ResponseEntity<List<ActivityResponse>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('activity.view') or hasRole('ADMIN')")
    public ResponseEntity<List<ActivityResponse>> getActive() {

        return ResponseEntity.ok(service.getActive());
    }

    @GetMapping("/default")
    @PreAuthorize("hasAuthority('activity.view') or hasRole('ADMIN')")
    public ResponseEntity<List<ActivityResponse>> getDefault() {

        return ResponseEntity.ok(service.getDefault());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('activity.view') or hasRole('ADMIN')")
    public ResponseEntity<ActivityResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('activity.create') or hasRole('ADMIN')")
    public ResponseEntity<ActivityResponse> create(
            @Valid @RequestBody ActivityRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('activity.update') or hasRole('ADMIN')")
    public ResponseEntity<ActivityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('activity.update') or hasRole('ADMIN')")
    public ResponseEntity<ActivityResponse> restore(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.restore(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('activity.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasAuthority('activity.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> permanentlyDelete(
            @PathVariable Long id) {

        service.permanentlyDelete(id);

        return ResponseEntity.noContent().build();
    }
}