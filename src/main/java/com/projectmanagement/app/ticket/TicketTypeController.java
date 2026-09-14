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
@RequestMapping("/api/ticket-types")
public class TicketTypeController {

        private final TicketTypeService ticketTypeService;

        public TicketTypeController(
                        TicketTypeService ticketTypeService) {
                this.ticketTypeService = ticketTypeService;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketTypeResponse>> getAllTicketTypes() {

                return ResponseEntity.ok(
                                ticketTypeService.getAllTicketTypes());
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketTypeResponse>> getActiveTicketTypes() {

                return ResponseEntity.ok(
                                ticketTypeService.getActiveTicketTypes());
        }

        // =========================================================
        // GET DEFAULT
        // =========================================================

        @GetMapping("/default")
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketTypeResponse>> getDefaultTicketTypes() {

                return ResponseEntity.ok(
                                ticketTypeService.getDefaultTicketTypes());
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketTypeResponse> getTicketTypeById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketTypeService.getTicketTypeById(id));
        }

        // =========================================================
        // GET BY NAME
        // =========================================================

        @GetMapping("/name/{name}")
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketTypeResponse> getTicketTypeByName(
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                ticketTypeService.getTicketTypeByName(name));
        }

        // =========================================================
        // GET BY ICON
        // =========================================================

        @GetMapping("/icon/{icon}")
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketTypeResponse>> getTicketTypesByIcon(
                        @PathVariable String icon) {

                return ResponseEntity.ok(
                                ticketTypeService.getTicketTypesByIcon(icon));
        }

        // =========================================================
        // GET BY COLOR
        // =========================================================

        @GetMapping("/color/{color}")
        @PreAuthorize("hasAuthority('ticket_type.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketTypeResponse>> getTicketTypesByColor(
                        @PathVariable String color) {

                return ResponseEntity.ok(
                                ticketTypeService.getTicketTypesByColor(color));
        }

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_type.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketTypeResponse> createTicketType(
                        @Valid @RequestBody TicketTypeRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ticketTypeService
                                                                .createTicketType(request));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_type.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketTypeResponse> updateTicketType(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketTypeRequest request) {

                return ResponseEntity.ok(
                                ticketTypeService.updateTicketType(
                                                id,
                                                request));
        }

        // =========================================================
        // RESTORE
        // =========================================================

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('ticket_type.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketTypeResponse> restoreTicketType(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketTypeService.restoreTicketType(id));
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_type.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteTicketType(
                        @PathVariable Long id) {

                ticketTypeService.deleteTicketType(id);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('ticket_type.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentlyDeleteTicketType(
                        @PathVariable Long id) {

                ticketTypeService
                                .permanentlyDeleteTicketType(id);

                return ResponseEntity.noContent().build();
        }
}