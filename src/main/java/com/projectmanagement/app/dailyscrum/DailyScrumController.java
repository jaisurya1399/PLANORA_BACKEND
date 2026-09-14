package com.projectmanagement.app.dailyscrum;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.auth.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/daily-scrums")
@Validated
public class DailyScrumController {

        private final DailyScrumService dailyScrumService;
        private final CurrentUserService currentUserService;

        public DailyScrumController(
                        DailyScrumService dailyScrumService,
                        CurrentUserService currentUserService) {

                this.dailyScrumService = dailyScrumService;
                this.currentUserService = currentUserService;
        }

        // ============================================================
        // CREATE DAILY SCRUM
        // ============================================================

        @PostMapping
        public ResponseEntity<DailyScrumResponse> create(
                        Authentication authentication,
                        @Valid @RequestBody DailyScrumRequest request) {

                Long loggedInUserId = currentUserService.getCurrentUserId();

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                dailyScrumService.create(
                                                                loggedInUserId,
                                                                request));
        }

        // ============================================================
        // GET ALL DAILY SCRUMS
        // ============================================================

        @GetMapping
        public ResponseEntity<List<DailyScrumResponse>> getAll() {

                return ResponseEntity.ok(
                                dailyScrumService.getAll());
        }

        // ============================================================
        // GET DAILY SCRUM BY ID
        // ============================================================

        @GetMapping("/{id}")
        public ResponseEntity<DailyScrumResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                dailyScrumService.getById(id));
        }

        // ============================================================
        // GET DAILY SCRUMS BY USER
        // ============================================================

        @GetMapping("/user/{userId}")
        public ResponseEntity<List<DailyScrumResponse>> getByUser(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                dailyScrumService.getByUser(userId));
        }

        // ============================================================
        // GET DAILY SCRUMS BY USER + DATE RANGE
        // ============================================================

        @GetMapping("/user/{userId}/range")
        public ResponseEntity<List<DailyScrumResponse>> getByUserAndRange(
                        @PathVariable Long userId,
                        @RequestParam LocalDate startDate,
                        @RequestParam LocalDate endDate) {

                return ResponseEntity.ok(
                                dailyScrumService.getByUserAndRange(
                                                userId,
                                                startDate,
                                                endDate));
        }

        // ============================================================
        // GET DAILY SCRUMS BY DATE RANGE
        // ============================================================

        @GetMapping("/range")
        public ResponseEntity<List<DailyScrumResponse>> getByDateRange(
                        @RequestParam LocalDate startDate,
                        @RequestParam LocalDate endDate) {

                return ResponseEntity.ok(
                                dailyScrumService.getByDateRange(
                                                startDate,
                                                endDate));
        }

        // ============================================================
        // GET DAILY SCRUMS BY PROJECT + DATE RANGE
        // ============================================================

        @GetMapping("/project/{projectId}/range")
        public ResponseEntity<List<DailyScrumResponse>> getByProjectAndRange(
                        @PathVariable Long projectId,
                        @RequestParam LocalDate startDate,
                        @RequestParam LocalDate endDate) {

                return ResponseEntity.ok(
                                dailyScrumService.getByProjectAndRange(
                                                projectId,
                                                startDate,
                                                endDate));
        }

        // ============================================================
        // UPDATE DAILY SCRUM
        // ============================================================

        @PutMapping("/{id}")
        public ResponseEntity<DailyScrumResponse> update(
                        Authentication authentication,
                        @PathVariable Long id,
                        @Valid @RequestBody DailyScrumRequest request) {

                Long loggedInUserId = currentUserService.getCurrentUserId();

                return ResponseEntity.ok(
                                dailyScrumService.update(
                                                loggedInUserId,
                                                id,
                                                request));
        }

        // ============================================================
        // DELETE DAILY SCRUM
        // ============================================================

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        Authentication authentication,
                        @PathVariable Long id) {

                Long loggedInUserId = currentUserService.getCurrentUserId();

                dailyScrumService.delete(
                                loggedInUserId,
                                id);

                return ResponseEntity.noContent().build();
        }
}