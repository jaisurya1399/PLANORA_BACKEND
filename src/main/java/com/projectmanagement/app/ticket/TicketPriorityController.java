package com.projectmanagement.app.ticket;

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

@RestController
@RequestMapping("/api/ticket-priorities")
public class TicketPriorityController {

        private final TicketPriorityService ticketPriorityService;

        public TicketPriorityController(
                        TicketPriorityService ticketPriorityService) {

                this.ticketPriorityService = ticketPriorityService;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_priority.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketPriorityResponse>> getAll() {

                return ResponseEntity.ok(
                                ticketPriorityService.getAll());
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('ticket_priority.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketPriorityResponse>> getAllActive() {

                return ResponseEntity.ok(
                                ticketPriorityService.getAllActive());
        }

        // =========================================================
        // GET DEFAULT
        // =========================================================

        @GetMapping("/default")
        @PreAuthorize("hasAuthority('ticket_priority.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketPriorityResponse>> getDefaultPriorities() {

                return ResponseEntity.ok(
                                ticketPriorityService.getDefaultPriorities());
        }

        // =========================================================
        // GET BY NAME
        // =========================================================

        @GetMapping("/name/{name}")
        @PreAuthorize("hasAuthority('ticket_priority.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketPriorityResponse> getByName(
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                ticketPriorityService.getByName(name));
        }

        // =========================================================
        // GET BY COLOR
        // =========================================================

        @GetMapping("/color/{color}")
        @PreAuthorize("hasAuthority('ticket_priority.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketPriorityResponse>> getByColor(
                        @PathVariable String color) {

                return ResponseEntity.ok(
                                ticketPriorityService.getByColor(color));
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_priority.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketPriorityResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketPriorityService.getById(id));
        }

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_priority.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketPriorityResponse> create(
                        @Valid @RequestBody TicketPriorityRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ticketPriorityService.create(request));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_priority.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketPriorityResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketPriorityRequest request) {

                return ResponseEntity.ok(
                                ticketPriorityService.update(
                                                id,
                                                request));
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_priority.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                ticketPriorityService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/reorder")
        @PreAuthorize("hasAuthority('ticket_priority.update') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketPriorityResponse>> reorder(@RequestBody List<Long> ids) {
                return ResponseEntity.ok(ticketPriorityService.reorder(ids));
        }

        // =========================================================
        // RESTORE
        // =========================================================

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('ticket_priority.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketPriorityResponse> restore(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketPriorityService.restore(id));
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('ticket_priority.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentDelete(
                        @PathVariable Long id) {

                ticketPriorityService.permanentDelete(id);

                return ResponseEntity.noContent().build();
        }
}