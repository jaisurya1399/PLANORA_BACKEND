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
@RequestMapping("/api/ticket-subscribers")
@RequiredArgsConstructor
public class TicketSubscriberController {

        private final TicketSubscriberService ticketSubscriberService;

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketSubscriberResponse>> getAll() {

                return ResponseEntity.ok(
                                ticketSubscriberService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketSubscriberResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketSubscriberService.getById(id));
        }

        @GetMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketSubscriberResponse>> getByTicket(
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                ticketSubscriberService.getByTicket(ticketId));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketSubscriberResponse>> getByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketSubscriberService.getByUser(userId));
        }

        @GetMapping("/ticket/{ticketId}/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketSubscriberResponse> getByTicketAndUser(
                        @PathVariable Long ticketId,
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketSubscriberService.getByTicketAndUser(
                                                ticketId,
                                                userId));
        }

        @GetMapping("/ticket/{ticketId}/user/{userId}/exists")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<Boolean> exists(
                        @PathVariable Long ticketId,
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketSubscriberService.exists(ticketId, userId));
        }

        @GetMapping("/ticket/{ticketId}/count")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByTicket(
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                ticketSubscriberService.countByTicket(ticketId));
        }

        @GetMapping("/user/{userId}/count")
        @PreAuthorize("hasAuthority('ticket_subscriber.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketSubscriberService.countByUser(userId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_subscriber.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketSubscriberResponse> create(
                        @Valid @RequestBody TicketSubscriberRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ticketSubscriberService.create(request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_subscriber.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                ticketSubscriberService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/ticket/{ticketId}/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_subscriber.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> unsubscribe(
                        @PathVariable Long ticketId,
                        @PathVariable Long userId) {

                ticketSubscriberService.unsubscribe(
                                ticketId,
                                userId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_subscriber.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByTicket(
                        @PathVariable Long ticketId) {

                ticketSubscriberService.deleteByTicket(ticketId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_subscriber.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByUser(
                        @PathVariable Long userId) {

                ticketSubscriberService.deleteByUser(userId);

                return ResponseEntity.noContent().build();
        }
}