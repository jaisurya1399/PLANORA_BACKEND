package com.projectmanagement.app.epic;

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
@RequestMapping("/api/epics")
public class EpicController {

        private final EpicService epicService;

        public EpicController(
                        EpicService epicService) {
                this.epicService = epicService;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @GetMapping
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getAllEpics() {

                return ResponseEntity.ok(
                                epicService.getAllEpics());
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getActiveEpics() {

                return ResponseEntity.ok(
                                epicService.getActiveEpics());
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<EpicResponse> getEpicById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                epicService.getEpicById(id));
        }

        // =========================================================
        // GET BY PROJECT
        // =========================================================

        @GetMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getEpicsByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                epicService.getEpicsByProject(projectId));
        }

        // =========================================================
        // GET ACTIVE BY PROJECT
        // =========================================================

        @GetMapping("/project/{projectId}/active")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getActiveEpicsByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                epicService.getActiveEpicsByProject(
                                                projectId));
        }

        // =========================================================
        // GET ROOT EPICS
        // =========================================================

        @GetMapping("/project/{projectId}/root")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getRootEpicsByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                epicService.getRootEpicsByProject(
                                                projectId));
        }

        // =========================================================
        // GET CHILD EPICS
        // =========================================================

        @GetMapping("/{parentId}/children")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getChildEpics(
                        @PathVariable Long parentId) {

                return ResponseEntity.ok(
                                epicService.getChildEpics(parentId));
        }

        // =========================================================
        // GET ACTIVE CHILD EPICS
        // =========================================================

        @GetMapping("/{parentId}/children/active")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getActiveChildEpics(
                        @PathVariable Long parentId) {

                return ResponseEntity.ok(
                                epicService.getActiveChildEpics(parentId));
        }

        // =========================================================
        // GET BY PROJECT + PARENT
        // =========================================================

        @GetMapping("/project/{projectId}/parent/{parentId}")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<List<EpicResponse>> getEpicsByProjectAndParent(
                        @PathVariable Long projectId,
                        @PathVariable Long parentId) {

                return ResponseEntity.ok(
                                epicService.getEpicsByProjectAndParent(
                                                projectId,
                                                parentId));
        }

        // =========================================================
        // GET BY PROJECT + NAME
        // =========================================================

        @GetMapping("/project/{projectId}/name/{name}")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<EpicResponse> getEpicByProjectAndName(
                        @PathVariable Long projectId,
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                epicService.getEpicByProjectAndName(
                                                projectId,
                                                name));
        }

        // =========================================================
        // EPIC PROGRESS
        // =========================================================

        @GetMapping("/{id}/progress")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<EpicProgressResponse> getEpicProgress(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                epicService.getEpicProgress(id));
        }

        // =========================================================
        // EPIC BURNDOWN
        // =========================================================

        @GetMapping("/{id}/burndown")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<EpicBurndownResponse> getEpicBurndown(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                epicService.getEpicBurndown(id));
        }

        // =========================================================
        // EPIC REPORT
        // =========================================================

        @GetMapping("/{id}/report")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<EpicReportResponse> getEpicReport(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                epicService.getEpicReport(id));
        }

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping
        @PreAuthorize("hasAuthority('epic.create') or hasRole('ADMIN')")
        public ResponseEntity<EpicResponse> createEpic(
                        @Valid @RequestBody EpicRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                epicService.createEpic(request));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('epic.update') or hasRole('ADMIN')")
        public ResponseEntity<EpicResponse> updateEpic(
                        @PathVariable Long id,
                        @Valid @RequestBody EpicRequest request) {

                return ResponseEntity.ok(
                                epicService.updateEpic(
                                                id,
                                                request));
        }

        // =========================================================
        // RESTORE
        // =========================================================

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('epic.update') or hasRole('ADMIN')")
        public ResponseEntity<EpicResponse> restoreEpic(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                epicService.restoreEpic(id));
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('epic.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteEpic(
                        @PathVariable Long id) {

                epicService.deleteEpic(id);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('epic.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentlyDeleteEpic(
                        @PathVariable Long id) {

                epicService.permanentlyDeleteEpic(id);

                return ResponseEntity.noContent().build();
        }

        // =========================================================
        // COUNT BY PROJECT
        // =========================================================

        @GetMapping("/project/{projectId}/count")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countEpicsByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                epicService.countEpicsByProject(projectId));
        }

        // =========================================================
        // COUNT CHILDREN
        // =========================================================

        @GetMapping("/{parentId}/children/count")
        @PreAuthorize("hasAuthority('epic.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countChildEpics(
                        @PathVariable Long parentId) {

                return ResponseEntity.ok(
                                epicService.countChildEpics(parentId));
        }
}