package com.projectmanagement.app.project;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-statuses")
public class ProjectStatusController {

        private final ProjectStatusService projectStatusService;

        public ProjectStatusController(
                        ProjectStatusService projectStatusService) {
                this.projectStatusService = projectStatusService;
        }

        // ---------------------------------------------------------
        // GET ALL
        // ---------------------------------------------------------

        @GetMapping
        @PreAuthorize("hasAuthority('project_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectStatusResponse>> getAllProjectStatuses() {

                return ResponseEntity.ok(
                                projectStatusService.getAllProjectStatuses());
        }

        // ---------------------------------------------------------
        // GET ACTIVE
        // ---------------------------------------------------------

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('project_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectStatusResponse>> getActiveProjectStatuses() {

                return ResponseEntity.ok(
                                projectStatusService.getActiveProjectStatuses());
        }

        // ---------------------------------------------------------
        // GET DEFAULT
        // ---------------------------------------------------------

        @GetMapping("/default")
        @PreAuthorize("hasAuthority('project_status.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectStatusResponse>> getDefaultProjectStatuses() {

                return ResponseEntity.ok(
                                projectStatusService.getDefaultProjectStatuses());
        }

        // ---------------------------------------------------------
        // GET BY ID
        // ---------------------------------------------------------

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('project_status.view') or hasRole('ADMIN')")
        public ResponseEntity<ProjectStatusResponse> getProjectStatusById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                projectStatusService.getProjectStatusById(id));
        }

        // ---------------------------------------------------------
        // GET BY NAME
        // ---------------------------------------------------------

        @GetMapping("/name/{name}")
        @PreAuthorize("hasAuthority('project_status.view') or hasRole('ADMIN')")
        public ResponseEntity<ProjectStatusResponse> getProjectStatusByName(
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                projectStatusService.getProjectStatusByName(name));
        }

        // ---------------------------------------------------------
        // CREATE
        // ---------------------------------------------------------

        @PostMapping
        @PreAuthorize("hasAuthority('project_status.create') or hasRole('ADMIN')")
        public ResponseEntity<ProjectStatusResponse> createProjectStatus(
                        @Valid @RequestBody ProjectStatusRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                projectStatusService
                                                                .createProjectStatus(request));
        }

        // ---------------------------------------------------------
        // UPDATE
        // ---------------------------------------------------------

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('project_status.update') or hasRole('ADMIN')")
        public ResponseEntity<ProjectStatusResponse> updateProjectStatus(
                        @PathVariable Long id,
                        @Valid @RequestBody ProjectStatusRequest request) {

                return ResponseEntity.ok(
                                projectStatusService.updateProjectStatus(
                                                id,
                                                request));
        }

        // ---------------------------------------------------------
        // RESTORE
        // ---------------------------------------------------------

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('project_status.update') or hasRole('ADMIN')")
        public ResponseEntity<ProjectStatusResponse> restoreProjectStatus(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                projectStatusService
                                                .restoreProjectStatus(id));
        }

        // ---------------------------------------------------------
        // SOFT DELETE
        // ---------------------------------------------------------

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('project_status.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteProjectStatus(
                        @PathVariable Long id) {

                projectStatusService.deleteProjectStatus(id);

                return ResponseEntity.noContent().build();
        }

        // ---------------------------------------------------------
        // PERMANENT DELETE
        // ---------------------------------------------------------

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('project_status.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentlyDeleteProjectStatus(
                        @PathVariable Long id) {

                projectStatusService
                                .permanentlyDeleteProjectStatus(id);

                return ResponseEntity.noContent().build();
        }
}