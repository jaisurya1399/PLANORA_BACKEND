package com.projectmanagement.app.milestone;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MilestoneController {

        private final MilestoneService milestoneService;

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping("/projects/{projectId}/milestones")
        public ResponseEntity<MilestoneResponse> create(
                        @PathVariable Long projectId,
                        @Valid @RequestBody MilestoneRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                milestoneService.create(
                                                                projectId,
                                                                request));
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @GetMapping("/projects/{projectId}/milestones")
        public ResponseEntity<List<MilestoneResponse>> getAll(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                milestoneService.getProjectMilestones(
                                                projectId));
        }

        // =========================================================
        // GET ONE
        // =========================================================

        @GetMapping("/projects/{projectId}/milestones/{milestoneId}")
        public ResponseEntity<MilestoneResponse> get(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId) {

                return ResponseEntity.ok(
                                milestoneService.get(
                                                projectId,
                                                milestoneId));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/projects/{projectId}/milestones/{milestoneId}")
        public ResponseEntity<MilestoneResponse> update(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId,
                        @Valid @RequestBody MilestoneRequest request) {

                return ResponseEntity.ok(
                                milestoneService.update(
                                                projectId,
                                                milestoneId,
                                                request));
        }

        // =========================================================
        // DELETE
        // =========================================================

        @DeleteMapping("/projects/{projectId}/milestones/{milestoneId}")
        public ResponseEntity<Void> delete(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId) {

                milestoneService.delete(
                                projectId,
                                milestoneId);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // STATUS
        // =========================================================

        @PatchMapping("/projects/{projectId}/milestones/{milestoneId}/status")
        public ResponseEntity<MilestoneResponse> updateStatus(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId,
                        @RequestParam MilestoneStatus status) {

                return ResponseEntity.ok(
                                milestoneService.updateStatus(
                                                projectId,
                                                milestoneId,
                                                status));
        }

        // =========================================================
        // PROGRESS
        // =========================================================

        @PatchMapping("/projects/{projectId}/milestones/{milestoneId}/progress")
        public ResponseEntity<MilestoneResponse> updateProgress(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId,
                        @RequestParam @Min(0) @Max(100) Integer progress) {

                return ResponseEntity.ok(
                                milestoneService.updateProgress(
                                                projectId,
                                                milestoneId,
                                                progress));
        }

        // =========================================================
        // ASSIGN TICKET
        // =========================================================

        @PostMapping("/projects/{projectId}/milestones/{milestoneId}/tickets/{ticketId}")
        public ResponseEntity<MilestoneResponse> assignTicket(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId,
                        @PathVariable Long ticketId) {

                return ResponseEntity.ok(
                                milestoneService.assignTicket(
                                                projectId,
                                                milestoneId,
                                                ticketId));
        }

        // =========================================================
        // REMOVE TICKET
        // =========================================================

        @DeleteMapping("/projects/{projectId}/milestones/{milestoneId}/tickets/{ticketId}")
        public ResponseEntity<Void> removeTicket(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId,
                        @PathVariable Long ticketId) {

                milestoneService.removeTicket(
                                projectId,
                                milestoneId,
                                ticketId);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // GET MILESTONE TICKETS
        // =========================================================

        @GetMapping("/projects/{projectId}/milestones/{milestoneId}/tickets")
        public ResponseEntity<List<MilestoneTicketResponse>> getTickets(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId) {

                return ResponseEntity.ok(
                                milestoneService.getTickets(
                                                projectId,
                                                milestoneId));
        }

        // =========================================================
        // STATISTICS
        // =========================================================

        @GetMapping("/projects/{projectId}/milestones/{milestoneId}/statistics")
        public ResponseEntity<MilestoneStatisticsResponse> getStatistics(
                        @PathVariable Long projectId,
                        @PathVariable Long milestoneId) {

                return ResponseEntity.ok(
                                milestoneService.getStatistics(
                                                projectId,
                                                milestoneId));
        }
}