package com.projectmanagement.app.sprint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.ticket.TicketResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

        private final SprintService sprintService;
        private final CurrentUserService currentUserService;

        public SprintController(
                        SprintService sprintService,
                        CurrentUserService currentUserService) {
                this.sprintService = sprintService;
                this.currentUserService = currentUserService;
        }

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping
        public ResponseEntity<SprintResponse> create(
                        @Valid @RequestBody SprintRequest request,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                sprintService.create(
                                                                request,
                                                                userId));
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @GetMapping
        public ResponseEntity<List<SprintResponse>> getAll() {

                return ResponseEntity.ok(
                                sprintService.getAll());
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @GetMapping("/{id}")
        public ResponseEntity<SprintResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                sprintService.getById(id));
        }

        // =========================================================
        // GET PROJECT SPRINTS
        // =========================================================

        @GetMapping("/project/{projectId}")
        public ResponseEntity<List<SprintResponse>> getByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                sprintService.getByProject(
                                                projectId));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/{id}")
        public ResponseEntity<SprintResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody SprintRequest request,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.update(
                                                id,
                                                request,
                                                userId));
        }

        // =========================================================
        // DELETE
        // =========================================================

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                sprintService.delete(
                                id,
                                userId);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // START
        // =========================================================

        @PostMapping("/{id}/start")
        public ResponseEntity<SprintResponse> start(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.start(
                                                id,
                                                userId));
        }

        // =========================================================
        // COMPLETE
        // =========================================================

        @PostMapping("/{id}/complete")
        public ResponseEntity<SprintResponse> complete(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.complete(
                                                id,
                                                userId));
        }

        @PostMapping("/{id}/complete-with-carry-over")
        public ResponseEntity<SprintResponse> completeWithCarryOver(@PathVariable Long id,
                        @Valid @RequestBody SprintCompletionRequest request, Authentication authentication) {
                return ResponseEntity.ok(sprintService.complete(id, request, getUserId(authentication)));
        }

        @GetMapping("/{id}/burndown")
        public ResponseEntity<SprintBurndownResponse> burndown(@PathVariable Long id) {
                return ResponseEntity.ok(sprintService.burndown(id));
        }

        // =========================================================
        // CANCEL
        // =========================================================

        @PostMapping("/{id}/cancel")
        public ResponseEntity<SprintResponse> cancel(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.cancel(
                                                id,
                                                userId));
        }

        // =========================================================
        // SPRINT TICKETS
        // =========================================================

        @GetMapping("/{id}/tickets")
        public ResponseEntity<List<TicketResponse>> getSprintTickets(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                sprintService.getSprintTickets(id));
        }

        // =========================================================
        // PROJECT BACKLOG
        // =========================================================

        @GetMapping("/project/{projectId}/backlog")
        public ResponseEntity<List<TicketResponse>> getBacklog(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                sprintService.getBacklog(
                                                projectId));
        }

        // =========================================================
        // ADD TICKET
        // =========================================================

        @PostMapping("/{sprintId}/tickets/{ticketId}")
        public ResponseEntity<TicketResponse> addTicket(
                        @PathVariable Long sprintId,
                        @PathVariable Long ticketId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.addTicket(
                                                sprintId,
                                                ticketId,
                                                userId));
        }

        // =========================================================
        // REMOVE TICKET
        // =========================================================

        @DeleteMapping("/{sprintId}/tickets/{ticketId}")
        public ResponseEntity<TicketResponse> removeTicket(
                        @PathVariable Long sprintId,
                        @PathVariable Long ticketId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.removeTicket(
                                                sprintId,
                                                ticketId,
                                                userId));
        }

        // =========================================================
        // MOVE TO BACKLOG
        // =========================================================

        @PostMapping("/tickets/{ticketId}/backlog")
        public ResponseEntity<TicketResponse> moveToBacklog(
                        @PathVariable Long ticketId,
                        Authentication authentication) {

                Long userId = getUserId(authentication);

                return ResponseEntity.ok(
                                sprintService.moveToBacklog(
                                                ticketId,
                                                userId));
        }

        // =========================================================
        // STATISTICS
        // =========================================================

        @GetMapping("/{id}/statistics")
        public ResponseEntity<SprintStatisticsResponse> statistics(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                sprintService.statistics(id));
        }

        @GetMapping("/project/{projectId}/history")
        public ResponseEntity<List<SprintHistoryResponse>> history(@PathVariable Long projectId) {
                return ResponseEntity.ok(sprintService.history(projectId));
        }

        @GetMapping("/project/{projectId}/velocity")
        public ResponseEntity<SprintVelocityResponse> velocity(@PathVariable Long projectId) {
                return ResponseEntity.ok(sprintService.velocity(projectId));
        }

        @GetMapping("/{id}/report")
        public ResponseEntity<SprintReportResponse> report(@PathVariable Long id) {
                return ResponseEntity.ok(sprintService.report(id));
        }

        @GetMapping("/{id}/commitment")
        public ResponseEntity<SprintCommitmentResponse> commitment(@PathVariable Long id) {
                return ResponseEntity.ok(sprintService.commitment(id));
        }

        @GetMapping("/{id}/capacity")
        public ResponseEntity<List<SprintCapacityResponse>> capacity(@PathVariable Long id) {
                return ResponseEntity.ok(sprintService.getCapacity(id));
        }

        @PutMapping("/{id}/capacity")
        public ResponseEntity<SprintCapacityResponse> saveCapacity(
                        @PathVariable Long id,
                        @Valid @RequestBody SprintCapacityRequest request,
                        Authentication authentication) {
                return ResponseEntity.ok(sprintService.saveCapacity(id, request, getUserId(authentication)));
        }

        // =========================================================
        // USER ID
        // =========================================================

        private Long getUserId(
                        Authentication authentication) {
                return currentUserService.getCurrentUserId();
        }
}
