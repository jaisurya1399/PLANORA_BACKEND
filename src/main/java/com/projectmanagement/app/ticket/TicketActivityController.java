package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-activities")
@RequiredArgsConstructor
public class TicketActivityController {

        private final TicketActivityService service;

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_activity.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketActivityResponse>> getAll() {
                return ResponseEntity.ok(service.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_activity.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketActivityResponse> getById(
                        @PathVariable Long id) {
                return ResponseEntity.ok(service.getById(id));
        }

        @GetMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_activity.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketActivityResponse>> getByTicket(
                        @PathVariable Long ticketId) {
                return ResponseEntity.ok(
                                service.getByTicket(ticketId));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_activity.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketActivityResponse>> getByUser(
                        @PathVariable Long userId) {
                return ResponseEntity.ok(
                                service.getByUser(userId));
        }

        @GetMapping("/ticket/{ticketId}/count")
        @PreAuthorize("hasAuthority('ticket_activity.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByTicket(
                        @PathVariable Long ticketId) {
                return ResponseEntity.ok(
                                service.countByTicket(ticketId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_activity.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketActivityResponse> create(
                        @Valid @RequestBody TicketActivityRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(service.create(request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_activity.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {
                service.delete(id);
                return ResponseEntity.noContent().build();
        }
}