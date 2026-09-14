package com.projectmanagement.app.ticket;

import java.util.List;

import jakarta.validation.Valid;

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

@RestController
@RequestMapping("/api/ticket-statuses")
public class TicketStatusController {

        private final TicketStatusService ticketStatusService;

        public TicketStatusController(
                        TicketStatusService ticketStatusService) {

                this.ticketStatusService = ticketStatusService;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getAll() {

                return ResponseEntity.ok(
                                ticketStatusService.getAll());
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getAllActive() {

                return ResponseEntity.ok(
                                ticketStatusService.getAllActive());
        }

        // =========================================================
        // GET DEFAULT
        // =========================================================

        @GetMapping("/default")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getDefaultStatuses() {

                return ResponseEntity.ok(
                                ticketStatusService.getDefaultStatuses());
        }

        // =========================================================
        // GET GLOBAL STATUSES
        // =========================================================

        @GetMapping("/global")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getGlobalStatuses() {

                return ResponseEntity.ok(
                                ticketStatusService.getGlobalStatuses());
        }

        // =========================================================
        // GET ACTIVE GLOBAL STATUSES
        // =========================================================

        @GetMapping("/global/active")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getActiveGlobalStatuses() {

                return ResponseEntity.ok(
                                ticketStatusService.getActiveGlobalStatuses());
        }

        // =========================================================
        // GET BY PROJECT
        // =========================================================

        @GetMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketStatusService.getByProject(
                                                projectId));
        }

        // =========================================================
        // GET ACTIVE BY PROJECT
        // =========================================================

        @GetMapping("/project/{projectId}/active")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getActiveByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketStatusService.getActiveByProject(
                                                projectId));
        }

        // =========================================================
        // GET DEFAULT BY PROJECT
        // =========================================================

        @GetMapping("/project/{projectId}/default")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getDefaultByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketStatusService
                                                .getDefaultStatusesByProject(
                                                                projectId));
        }

        // =========================================================
        // GET BY NAME
        // =========================================================

        @GetMapping("/name/{name}")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketStatusResponse> getByName(
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                ticketStatusService.getByName(name));
        }

        // =========================================================
        // GET BY PROJECT + NAME
        // =========================================================

        @GetMapping("/project/{projectId}/name/{name}")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketStatusResponse> getByProjectAndName(
                        @PathVariable Long projectId,
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                ticketStatusService.getByProjectAndName(
                                                projectId,
                                                name));
        }

        // =========================================================
        // GET BY COLOR
        // =========================================================

        @GetMapping("/color/{color}")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketStatusResponse>> getByColor(
                        @PathVariable String color) {

                return ResponseEntity.ok(
                                ticketStatusService.getByColor(color));
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_status.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketStatusResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketStatusService.getById(id));
        }

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_status.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketStatusResponse> create(
                        @Valid @RequestBody TicketStatusRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ticketStatusService.create(request));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_status.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketStatusResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketStatusRequest request) {

                return ResponseEntity.ok(
                                ticketStatusService.update(
                                                id,
                                                request));
        }

        // =========================================================
        // RESTORE
        // =========================================================

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('ticket_status.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketStatusResponse> restore(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketStatusService.restore(id));
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_status.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                ticketStatusService.delete(id);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('ticket_status.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentDelete(
                        @PathVariable Long id) {

                ticketStatusService.permanentDelete(id);

                return ResponseEntity.noContent().build();
        }
}