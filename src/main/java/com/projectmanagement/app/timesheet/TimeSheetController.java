package com.projectmanagement.app.timesheet;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time-sheets")
@RequiredArgsConstructor
public class TimeSheetController {

        private final TimeSheetService timeSheetService;

        @GetMapping
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetResponse>> getAll() {

                return ResponseEntity.ok(
                                timeSheetService.getAll());
        }

        @GetMapping("/active")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetResponse>> getActive() {

                return ResponseEntity.ok(
                                timeSheetService.getActive());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                timeSheetService.getById(id));
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetResponse>> getByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                timeSheetService.getByUser(userId));
        }

        @GetMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetResponse>> getByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                timeSheetService.getByProject(projectId));
        }

        @GetMapping("/user/{userId}/project/{projectId}")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetResponse>> getByUserAndProject(
                        @PathVariable Long userId,
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                timeSheetService.getByUserAndProject(
                                                userId,
                                                projectId));
        }

        @GetMapping("/search")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetResponse>> searchByTask(
                        @RequestParam String task) {

                return ResponseEntity.ok(
                                timeSheetService.searchByTask(task));
        }

        @GetMapping("/user/{userId}/count")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                timeSheetService.countByUser(userId));
        }

        @GetMapping("/project/{projectId}/count")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByProject(
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                timeSheetService.countByProject(projectId));
        }

        @GetMapping("/user/{userId}/project/{projectId}/count")
        @PreAuthorize("hasAuthority('timesheet.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByUserAndProject(
                        @PathVariable Long userId,
                        @PathVariable Long projectId) {

                return ResponseEntity.ok(
                                timeSheetService.countByUserAndProject(
                                                userId,
                                                projectId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('timesheet.create') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetResponse> create(
                        @Valid @RequestBody TimeSheetRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(timeSheetService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('timesheet.update') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TimeSheetRequest request) {

                return ResponseEntity.ok(
                                timeSheetService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('timesheet.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                timeSheetService.softDelete(id);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{id}/restore")
        @PreAuthorize("hasAuthority('timesheet.update') or hasRole('ADMIN')")
        public ResponseEntity<Void> restore(
                        @PathVariable Long id) {

                timeSheetService.restore(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id}/permanent")
        @PreAuthorize("hasAuthority('timesheet.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> permanentDelete(
                        @PathVariable Long id) {

                timeSheetService.permanentDelete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('timesheet.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByUser(
                        @PathVariable Long userId) {

                timeSheetService.deleteByUser(userId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/project/{projectId}")
        @PreAuthorize("hasAuthority('timesheet.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByProject(
                        @PathVariable Long projectId) {

                timeSheetService.deleteByProject(projectId);

                return ResponseEntity.noContent().build();
        }
}