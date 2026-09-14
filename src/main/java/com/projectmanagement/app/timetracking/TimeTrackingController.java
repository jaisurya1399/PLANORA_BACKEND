package com.projectmanagement.app.timetracking;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time-tracking")
@RequiredArgsConstructor
public class TimeTrackingController {
    private final TimeTrackingService service;

    @GetMapping("/ticket/{ticketId}/summary")
    @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
    public ResponseEntity<TimeTrackingSummaryResponse> summary(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.summary(ticketId));
    }

    @GetMapping("/timer")
    @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
    public ResponseEntity<TimeTrackingTimerResponse> activeTimer() {
        return ResponseEntity.ok(service.activeTimer());
    }

    @PostMapping("/timer/start/{ticketId}")
    @PreAuthorize("hasAuthority('ticket_hour.create') or hasRole('ADMIN')")
    public ResponseEntity<TimeTrackingTimerResponse> start(@PathVariable Long ticketId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(service.start(ticketId, description));
    }

    @PostMapping("/timer/stop")
    @PreAuthorize("hasAuthority('ticket_hour.create') or hasRole('ADMIN')")
    public ResponseEntity<?> stop() {
        return ResponseEntity.ok(service.stop());
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
    public ResponseEntity<List<TimeTrackingReportRow>> report(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.report(projectId, userId, from, to));
    }
}
