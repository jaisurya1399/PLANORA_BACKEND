package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/project-favorites")
@RequiredArgsConstructor
@Validated
public class ProjectFavoriteController {

        private final ProjectFavoriteService projectFavoriteService;

        @GetMapping
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectFavoriteResponse>> getAll() {

                return ResponseEntity.ok(
                                projectFavoriteService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<ProjectFavoriteResponse> getById(
                        @PathVariable @Positive Long id) {

                return ResponseEntity.ok(
                                projectFavoriteService.getById(id));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectFavoriteResponse>> getByUser(
                        @PathVariable @Positive Long userId) {

                return ResponseEntity.ok(
                                projectFavoriteService.getByUser(userId));
        }

        @GetMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ProjectFavoriteResponse>> getByProject(
                        @PathVariable @Positive Long projectId) {

                return ResponseEntity.ok(
                                projectFavoriteService.getByProject(projectId));
        }

        @GetMapping("/check")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<Boolean> exists(
                        @RequestParam @Positive Long userId,
                        @RequestParam @Positive Long projectId) {

                return ResponseEntity.ok(
                                projectFavoriteService.exists(
                                                userId,
                                                projectId));
        }

        @GetMapping("/user-project")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<ProjectFavoriteResponse> getByUserAndProject(
                        @RequestParam @Positive Long userId,
                        @RequestParam @Positive Long projectId) {

                return ResponseEntity.ok(
                                projectFavoriteService.getByUserAndProject(
                                                userId,
                                                projectId));
        }

        @GetMapping("/user/{userId}/count")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByUser(
                        @PathVariable @Positive Long userId) {

                return ResponseEntity.ok(
                                projectFavoriteService.countByUser(userId));
        }

        @GetMapping("/project/{projectId}/count")
        @PreAuthorize("hasAuthority('project_favorite.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByProject(
                        @PathVariable @Positive Long projectId) {

                return ResponseEntity.ok(
                                projectFavoriteService.countByProject(projectId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('project_favorite.create') or hasRole('ADMIN')")
        public ResponseEntity<ProjectFavoriteResponse> create(
                        @Valid @RequestBody ProjectFavoriteRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                projectFavoriteService.create(request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('project_favorite.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable @Positive Long id) {

                projectFavoriteService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user-project")
        @PreAuthorize("hasAuthority('project_favorite.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByUserAndProject(
                        @RequestParam @Positive Long userId,
                        @RequestParam @Positive Long projectId) {

                projectFavoriteService.deleteByUserAndProject(
                                userId,
                                projectId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('project_favorite.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByUser(
                        @PathVariable @Positive Long userId) {

                projectFavoriteService.deleteByUser(userId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('project_favorite.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByProject(
                        @PathVariable @Positive Long projectId) {

                projectFavoriteService.deleteByProject(projectId);

                return ResponseEntity.noContent().build();
        }
}