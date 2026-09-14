package com.projectmanagement.app.timesheet;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time-sheet-cells")
@RequiredArgsConstructor
@Validated
public class TimeSheetCellController {

        private final TimeSheetCellService timeSheetCellService;

        @GetMapping
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetCellResponse>> getAll() {
                return ResponseEntity.ok(
                                timeSheetCellService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetCellResponse> getById(
                        @PathVariable @Positive Long id) {
                return ResponseEntity.ok(
                                timeSheetCellService.getById(id));
        }

        @GetMapping("/time-sheet/{timeSheetId}")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetCellResponse>> getByTimeSheet(
                        @PathVariable @Positive Long timeSheetId) {
                return ResponseEntity.ok(
                                timeSheetCellService.getByTimeSheet(timeSheetId));
        }

        @GetMapping("/time-sheet/{timeSheetId}/desc")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetCellResponse>> getByTimeSheetDesc(
                        @PathVariable @Positive Long timeSheetId) {
                return ResponseEntity.ok(
                                timeSheetCellService.getByTimeSheetDesc(timeSheetId));
        }

        @GetMapping("/time-sheet/{timeSheetId}/date")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetCellResponse> getByTimeSheetAndDate(
                        @PathVariable @Positive Long timeSheetId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(
                                timeSheetCellService.getByTimeSheetAndDate(
                                                timeSheetId,
                                                date));
        }

        @GetMapping("/date")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetCellResponse>> getByDate(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(
                                timeSheetCellService.getByDate(date));
        }

        @GetMapping("/trips")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TimeSheetCellResponse>> getTripCells() {
                return ResponseEntity.ok(
                                timeSheetCellService.getTripCells());
        }

        @GetMapping("/time-sheet/{timeSheetId}/count")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByTimeSheet(
                        @PathVariable @Positive Long timeSheetId) {
                return ResponseEntity.ok(
                                timeSheetCellService.countByTimeSheet(timeSheetId));
        }

        @GetMapping("/time-sheet/{timeSheetId}/trip-count")
        @PreAuthorize("hasAuthority('timesheet_cell.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countTripCells(
                        @PathVariable @Positive Long timeSheetId) {
                return ResponseEntity.ok(
                                timeSheetCellService.countTripCells(timeSheetId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('timesheet_cell.create') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetCellResponse> create(
                        @Valid @RequestBody TimeSheetCellRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(timeSheetCellService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('timesheet_cell.update') or hasRole('ADMIN')")
        public ResponseEntity<TimeSheetCellResponse> update(
                        @PathVariable @Positive Long id,
                        @Valid @RequestBody TimeSheetCellRequest request) {
                return ResponseEntity.ok(
                                timeSheetCellService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('timesheet_cell.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable @Positive Long id) {
                timeSheetCellService.delete(id);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/time-sheet/{timeSheetId}")
        @PreAuthorize("hasAuthority('timesheet_cell.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteByTimeSheet(
                        @PathVariable @Positive Long timeSheetId) {
                timeSheetCellService.deleteByTimeSheet(timeSheetId);
                return ResponseEntity.noContent().build();
        }
}