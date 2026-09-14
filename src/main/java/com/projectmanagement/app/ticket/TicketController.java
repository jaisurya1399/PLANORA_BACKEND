package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

        private final TicketService ticketService;

        public TicketController(TicketService ticketService) {
                this.ticketService = ticketService;
        }

        @GetMapping
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getAll() {

                return ResponseEntity.ok(
                                ticketService.getAll());
        }

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getAllActive() {

                return ResponseEntity.ok(
                                ticketService.getAllActive());
        }

        @GetMapping("/resolved")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getResolved() {
                return ResponseEntity.ok(ticketService.getResolved());
        }

        @GetMapping("/deleted")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getDeleted() {

                return ResponseEntity.ok(
                                ticketService.getDeleted());
        }

        @GetMapping("/my-tasks")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getMyTasks() {

                return ResponseEntity.ok(
                                ticketService.getMyTasks());
        }

        @GetMapping("/my-tasks/all")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getMyTasksAll() {
                return ResponseEntity.ok(ticketService.getMyTasksAll());
        }

        @GetMapping("/my-tasks/resolved")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getMyResolvedTasks() {
                return ResponseEntity.ok(ticketService.getMyResolvedTasks());
        }

        @GetMapping("/code/{code}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketResponse> getByCode(
                        @PathVariable String code) {

                return ResponseEntity.ok(
                                ticketService.getByCode(code));
        }

        @GetMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketService.getByProject(projectId));
        }

        @GetMapping("/project/{projectId}/active")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getActiveByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketService.getActiveByProject(projectId));
        }

        @GetMapping("/project/{projectId}/resolved")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getResolvedByProject(@PathVariable Long projectId) {
                return ResponseEntity.ok(ticketService.getResolvedByProject(projectId));
        }

        @GetMapping("/project/{projectId}/root")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getRootTicketsByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketService.getRootTicketsByProject(projectId));
        }

        @GetMapping("/project/{projectId}/board")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<BoardColumnResponse>> getBoard(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                ticketService.getBoard(projectId));
        }

        @GetMapping("/{id:\\d+}/children")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getChildren(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketService.getChildren(id));
        }

        @GetMapping("/owner/{ownerId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByOwner(
                        @PathVariable Long ownerId) {

                return ResponseEntity.ok(
                                ticketService.getByOwner(ownerId));
        }

        @GetMapping("/responsible/{responsibleId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByResponsible(
                        @PathVariable Long responsibleId) {

                return ResponseEntity.ok(
                                ticketService.getByResponsible(responsibleId));
        }

        @GetMapping("/status/{statusId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByStatus(
                        @PathVariable Long statusId) {

                return ResponseEntity.ok(
                                ticketService.getByStatus(statusId));
        }

        @GetMapping("/type/{typeId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByType(
                        @PathVariable Long typeId) {

                return ResponseEntity.ok(
                                ticketService.getByType(typeId));
        }

        @GetMapping("/priority/{priorityId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByPriority(
                        @PathVariable Long priorityId) {

                return ResponseEntity.ok(
                                ticketService.getByPriority(priorityId));
        }

        @GetMapping("/epic/{epicId}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> getByEpic(
                        @PathVariable Long epicId) {

                return ResponseEntity.ok(
                                ticketService.getByEpic(epicId));
        }

        @GetMapping("/search")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> search(
                        @RequestParam String keyword) {

                return ResponseEntity.ok(
                                ticketService.search(keyword));
        }

        @GetMapping("/project/{projectId}/search")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> searchByProject(
                        @PathVariable Long projectId,
                        @RequestParam String keyword) {

                return ResponseEntity.ok(
                                ticketService.searchInProject(
                                                projectId,
                                                keyword));
        }

        @GetMapping("/project/{projectId}/filter")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketPageResponse> filter(
                        @PathVariable Long projectId,
                        @Valid @ModelAttribute TicketFilterRequest filter) {

                return ResponseEntity.ok(
                                ticketService.filter(
                                                projectId,
                                                filter));
        }

        @GetMapping("/{id:\\d+}")
        @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
        public ResponseEntity<TicketResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketService.getById(id));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ticket.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketResponse> create(
                        @Valid @RequestBody TicketRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ticketService.create(request));
        }

        @PutMapping("/{id:\\d+}")
        @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketRequest request) {

                return ResponseEntity.ok(
                                ticketService.update(id, request));
        }

        @PostMapping("/{id:\\d+}/remind-assignee")
        @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
        public ResponseEntity<Void> remindAssignee(@PathVariable Long id) {
                ticketService.remindAssignee(id);
                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{id:\\d+}/transition")
        @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketResponse> transition(
                        @PathVariable Long id,
                        @Valid @RequestBody TicketTransitionRequest request) {

                return ResponseEntity.ok(
                                ticketService.transition(id, request));
        }

        @PutMapping("/project/{projectId}/plan")
        @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketResponse>> plan(
                        @PathVariable Long projectId,
                        @Valid @RequestBody TicketPlanningRequest request) {

                return ResponseEntity.ok(
                                ticketService.plan(
                                                projectId,
                                                request));
        }

        @PutMapping("/{id:\\d+}/restore")
        @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
        public ResponseEntity<TicketResponse> restore(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ticketService.restore(id));
        }

        @DeleteMapping("/{id:\\d+}")
        @PreAuthorize("hasAuthority('ticket.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                ticketService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id:\\d+}/permanent")
        @PreAuthorize("hasAuthority('ticket.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentlyDelete(
                        @PathVariable Long id) {

                ticketService.permanentDelete(id);

                return ResponseEntity.noContent().build();
        }
}
