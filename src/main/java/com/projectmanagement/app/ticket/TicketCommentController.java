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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-comments")
@RequiredArgsConstructor
public class TicketCommentController {

        private final TicketCommentService ticketCommentService;

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketCommentResponse>> getAll() {
                return ResponseEntity.ok(
                                ticketCommentService.getAll());
        }

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketCommentResponse>> getActive() {
                return ResponseEntity.ok(
                                ticketCommentService.getActive());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketCommentResponse> getById(
                        @PathVariable Long id) {
                return ResponseEntity.ok(
                                ticketCommentService.getById(id));
        }

        @GetMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketCommentResponse>> getByTicket(
                        @PathVariable Long ticketId) {
                return ResponseEntity.ok(
                                ticketCommentService.getByTicket(ticketId));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketCommentResponse>> getByUser(
                        @PathVariable Long userId) {
                return ResponseEntity.ok(
                                ticketCommentService.getByUser(userId));
        }

        @GetMapping("/ticket/{ticketId}/count")
        @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByTicket(
                        @PathVariable Long ticketId) {
                return ResponseEntity.ok(
                                ticketCommentService.countByTicket(ticketId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_comment.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketCommentResponse> create(
                        @Valid @RequestBody TicketCommentRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ticketCommentService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_comment.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketCommentResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketCommentRequest request) {
                return ResponseEntity.ok(
                                ticketCommentService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_comment.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {
                ticketCommentService.softDelete(id);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('ticket_comment.update') or hasRole('ADMIN')")
        public ResponseEntity<Void> restore(
                        @PathVariable Long id) {
                ticketCommentService.restore(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('ticket_comment.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentDelete(
                        @PathVariable Long id) {
                ticketCommentService.permanentDelete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_comment.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByTicket(
                        @PathVariable Long ticketId) {
                ticketCommentService.deleteByTicket(ticketId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_comment.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByUser(
                        @PathVariable Long userId) {
                ticketCommentService.deleteByUser(userId);

                return ResponseEntity.noContent().build();
        }
}