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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-relations")
@RequiredArgsConstructor
public class TicketRelationController {

        private final TicketRelationService ticketRelationService;

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketRelationResponse>> getAll() {

                return ResponseEntity.ok(
                                ticketRelationService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketRelationResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketRelationService.getById(id));
        }

        @GetMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketRelationResponse>> getByTicket(
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                ticketRelationService.getByTicket(ticketId));
        }

        @GetMapping("/relation/{relationId}")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketRelationResponse>> getByRelation(
                        @PathVariable Long relationId) {

                return ResponseEntity.ok(
                                ticketRelationService.getByRelation(relationId));
        }

        @GetMapping("/type/{type}")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketRelationResponse>> getByType(
                        @PathVariable String type) {

                return ResponseEntity.ok(
                                ticketRelationService.getByType(type));
        }

        @GetMapping("/ticket/{ticketId}/type/{type}")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketRelationResponse>> getByTicketAndType(
                        @PathVariable Long ticketId,
                        @PathVariable String type) {

                return ResponseEntity.ok(
                                ticketRelationService.getByTicketAndType(
                                                ticketId,
                                                type));
        }

        @GetMapping("/exists")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<Boolean> exists(
                        @RequestParam Long ticketId,
                        @RequestParam Long relationId,
                        @RequestParam String type) {

                return ResponseEntity.ok(
                                ticketRelationService.exists(
                                                ticketId,
                                                relationId,
                                                type));
        }

        @GetMapping("/ticket/{ticketId}/count")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByTicket(
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                ticketRelationService.countByTicket(ticketId));
        }

        @GetMapping("/relation/{relationId}/count")
        @PreAuthorize("hasAuthority('ticket_relation.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByRelation(
                        @PathVariable Long relationId) {

                return ResponseEntity.ok(
                                ticketRelationService.countByRelation(relationId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_relation.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketRelationResponse> create(
                        @Valid @RequestBody TicketRelationRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ticketRelationService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_relation.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketRelationResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketRelationRequest request) {

                return ResponseEntity.ok(
                                ticketRelationService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_relation.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                ticketRelationService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_relation.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByTicket(
                        @PathVariable Long ticketId) {

                ticketRelationService.deleteByTicket(ticketId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/relation/{relationId}")
        @PreAuthorize("hasAuthority('ticket_relation.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByRelation(
                        @PathVariable Long relationId) {

                ticketRelationService.deleteByRelation(relationId);

                return ResponseEntity.noContent().build();
        }
}