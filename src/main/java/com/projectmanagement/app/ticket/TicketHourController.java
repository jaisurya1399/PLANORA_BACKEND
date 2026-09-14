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
@RequestMapping("/api/ticket-hours")
@RequiredArgsConstructor
public class TicketHourController {

        private final TicketHourService ticketHourService;

        @GetMapping
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketHourResponse>> getAll() {

                return ResponseEntity.ok(
                                ticketHourService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketHourResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketHourService.getById(id));
        }

        @GetMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketHourResponse>> getByTicket(
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                ticketHourService.getByTicket(ticketId));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketHourResponse>> getByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketHourService.getByUser(userId));
        }

        @GetMapping("/activity/{activityId}")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketHourResponse>> getByActivity(
                        @PathVariable Long activityId) {

                return ResponseEntity.ok(
                                ticketHourService.getByActivity(activityId));
        }

        @GetMapping("/ticket/{ticketId}/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketHourResponse>> getByTicketAndUser(
                        @PathVariable Long ticketId,
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketHourService.getByTicketAndUser(
                                                ticketId,
                                                userId));
        }

        @GetMapping("/ticket/{ticketId}/count")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByTicket(
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                ticketHourService.countByTicket(ticketId));
        }

        @GetMapping("/user/{userId}/count")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                ticketHourService.countByUser(userId));
        }

        @GetMapping("/activity/{activityId}/count")
        @PreAuthorize("hasAuthority('ticket_hour.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByActivity(
                        @PathVariable Long activityId) {

                return ResponseEntity.ok(
                                ticketHourService.countByActivity(activityId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ticket_hour.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketHourResponse> create(
                        @Valid @RequestBody TicketHourRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ticketHourService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_hour.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketHourResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketHourRequest request) {

                return ResponseEntity.ok(
                                ticketHourService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_hour.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                ticketHourService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_hour.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByTicket(
                        @PathVariable Long ticketId) {

                ticketHourService.deleteByTicket(ticketId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('ticket_hour.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByUser(
                        @PathVariable Long userId) {

                ticketHourService.deleteByUser(userId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/activity/{activityId}")
        @PreAuthorize("hasAuthority('ticket_hour.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByActivity(
                        @PathVariable Long activityId) {

                ticketHourService.deleteByActivity(activityId);

                return ResponseEntity.noContent().build();
        }
}