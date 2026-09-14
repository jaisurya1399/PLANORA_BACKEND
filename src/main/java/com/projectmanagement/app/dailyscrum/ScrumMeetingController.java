package com.projectmanagement.app.dailyscrum;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController @RequestMapping("/api/daily-scrum-meetings") @RequiredArgsConstructor @Validated
public class ScrumMeetingController {
    private final ScrumMeetingService service;
    @GetMapping @PreAuthorize("hasAuthority('daily_scrum.view') or hasRole('ADMIN')") public ResponseEntity<List<ScrumMeetingResponse>> getAll(){ return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/project/{projectId}") @PreAuthorize("hasAuthority('daily_scrum.view') or hasRole('ADMIN')") public ResponseEntity<ScrumMeetingResponse> getByProject(@PathVariable @Positive Long projectId){ return ResponseEntity.ok(service.getByProject(projectId)); }
    @PutMapping @PreAuthorize("hasAuthority('daily_scrum.update') or hasRole('ADMIN')") public ResponseEntity<ScrumMeetingResponse> save(@Valid @RequestBody ScrumMeetingRequest request){ return ResponseEntity.ok(service.save(request)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('daily_scrum.delete') or hasRole('ADMIN')") public ResponseEntity<Void> delete(@PathVariable @Positive Long id){ service.delete(id); return ResponseEntity.noContent().build(); }
}
