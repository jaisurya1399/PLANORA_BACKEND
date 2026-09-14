package com.projectmanagement.app.project;

import java.util.List;
import java.util.Map;

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

@RestController
@RequestMapping("/api/project-users")
public class ProjectUserController {

        private final ProjectUserService projectUserService;

        public ProjectUserController(ProjectUserService projectUserService) {
                this.projectUserService = projectUserService;
        }

        // ---------------------------------------------------------
        // GET ALL
        // ---------------------------------------------------------

        @GetMapping
        @PreAuthorize("hasAuthority('project_user.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectUserResponse>> getAllProjectUsers() {

                return ResponseEntity.ok(
                                projectUserService.getAllProjectUsers());
        }

        // ---------------------------------------------------------
        // GET BY ID
        // ---------------------------------------------------------

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('project_user.view') or hasRole('ADMIN')")
        public ResponseEntity<ProjectUserResponse> getProjectUserById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                projectUserService.getProjectUserById(id));
        }

        // ---------------------------------------------------------
        // GET BY PROJECT
        // ---------------------------------------------------------

        @GetMapping("/project/{projectId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<ProjectUserResponse>> getProjectUsersByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                projectUserService.getProjectUsersByProject(projectId));
        }

        // ---------------------------------------------------------
        // GET BY USER
        // ---------------------------------------------------------

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('project_user.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectUserResponse>> getProjectUsersByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                projectUserService.getProjectUsersByUser(userId));
        }

        // ---------------------------------------------------------
        // GET BY PROJECT + ROLE
        // ---------------------------------------------------------

        @GetMapping("/project/{projectId}/role/{role}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<ProjectUserResponse>> getProjectUsersByProjectAndRole(
                        @PathVariable Long projectId,
                        @PathVariable String role) {

                return ResponseEntity.ok(
                                projectUserService.getProjectUsersByProjectAndRole(
                                                projectId,
                                                role));
        }

        // ---------------------------------------------------------
        // CHECK ASSIGNMENT
        // ---------------------------------------------------------

        @GetMapping("/check")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Boolean> checkAssignment(
                        @RequestParam Long projectId,
                        @RequestParam Long userId) {

                return ResponseEntity.ok(
                                projectUserService.existsByProjectAndUser(
                                                projectId,
                                                userId));
        }

        // ---------------------------------------------------------
        // CREATE
        // ---------------------------------------------------------

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ProjectUserResponse> createProjectUser(
                        @Valid @RequestBody ProjectUserRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(projectUserService.createProjectUser(request));
        }

        // ---------------------------------------------------------
        // UPDATE
        // ---------------------------------------------------------

        @PutMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ProjectUserResponse> updateProjectUser(
                        @PathVariable Long id,
                        @Valid @RequestBody ProjectUserRequest request) {

                return ResponseEntity.ok(
                                projectUserService.updateProjectUser(
                                                id,
                                                request));
        }

        // ---------------------------------------------------------
        // OPEN / CLOSE MEMBER AVAILABILITY SELF UPDATE
        // ---------------------------------------------------------

        @PutMapping("/{id}/availability-self-update")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ProjectUserResponse> setAvailabilitySelfUpdate(
                        @PathVariable Long id,
                        @RequestBody Map<String, Boolean> request) {

                boolean enabled = Boolean.TRUE.equals(
                                request != null ? request.get("enabled") : Boolean.FALSE);

                return ResponseEntity.ok(
                                projectUserService.setAvailabilitySelfUpdateOpen(
                                                id,
                                                enabled));
        }

        // ---------------------------------------------------------
        // DELETE
        // ---------------------------------------------------------

        @DeleteMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Void> deleteProjectUser(
                        @PathVariable Long id) {

                projectUserService.deleteProjectUser(id);

                return ResponseEntity.noContent().build();
        }

        // ---------------------------------------------------------
        // DELETE ALL USERS FROM PROJECT
        // ---------------------------------------------------------

        @DeleteMapping("/project/{projectId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Void> deleteProjectUsersByProject(
                        @PathVariable Long projectId) {

                projectUserService.deleteProjectUsersByProject(projectId);

                return ResponseEntity.noContent().build();
        }

        // ---------------------------------------------------------
        // DELETE USER FROM ALL PROJECTS
        // ---------------------------------------------------------

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Void> deleteProjectUsersByUser(
                        @PathVariable Long userId) {

                projectUserService.deleteProjectUsersByUser(userId);

                return ResponseEntity.noContent().build();
        }

        // ---------------------------------------------------------
        // COUNT USERS IN PROJECT
        // ---------------------------------------------------------

        @GetMapping("/project/{projectId}/count")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Long> countUsersByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                projectUserService.countUsersByProject(projectId));
        }

        // ---------------------------------------------------------
        // COUNT PROJECTS FOR USER
        // ---------------------------------------------------------

        @GetMapping("/user/{userId}/count")
        @PreAuthorize("hasAuthority('project_user.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countProjectsByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                projectUserService.countProjectsByUser(userId));
        }
}