package com.projectmanagement.app.project;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/member-availability")
public class MemberAvailabilityController {

    private final MemberAvailabilityService service;

    public MemberAvailabilityController(MemberAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MemberAvailabilityResponse>> getProjectAvailability(
            @PathVariable Long projectId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(service.getProjectAvailability(projectId, startDate, endDate));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MemberAvailabilityResponse>> getUserAvailability(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(service.getUserAvailability(projectId, userId, startDate, endDate));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberAvailabilityResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody MemberAvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(projectId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberAvailabilityResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody MemberAvailabilityRequest request) {
        return ResponseEntity.ok(service.update(projectId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long id) {
        service.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }
}
