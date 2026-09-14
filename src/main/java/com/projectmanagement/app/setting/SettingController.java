package com.projectmanagement.app.setting;

import java.util.List;

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
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Validated
public class SettingController {

        private final SettingService settingService;

        @GetMapping
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<List<SettingResponse>> getAll() {
                return ResponseEntity.ok(
                                settingService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> getById(
                        @PathVariable @Positive Long id) {
                return ResponseEntity.ok(
                                settingService.getById(id));
        }

        @GetMapping("/group")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<List<SettingResponse>> getByGroup(
                        @RequestParam String group) {
                return ResponseEntity.ok(
                                settingService.getByGroup(group));
        }

        @GetMapping("/group/name")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> getByGroupAndName(
                        @RequestParam String group,
                        @RequestParam String name) {
                return ResponseEntity.ok(
                                settingService.getByGroupAndName(group, name));
        }

        @GetMapping("/name")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> getByName(
                        @RequestParam String name) {
                return ResponseEntity.ok(
                                settingService.getByName(name));
        }

        @GetMapping("/locked")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<List<SettingResponse>> getByLocked(
                        @RequestParam Boolean locked) {
                return ResponseEntity.ok(
                                settingService.getByLocked(locked));
        }

        @GetMapping("/group/locked")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<List<SettingResponse>> getByGroupAndLocked(
                        @RequestParam String group,
                        @RequestParam Boolean locked) {
                return ResponseEntity.ok(
                                settingService.getByGroupAndLocked(
                                                group,
                                                locked));
        }

        @GetMapping("/group/count")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByGroup(
                        @RequestParam String group) {
                return ResponseEntity.ok(
                                settingService.countByGroup(group));
        }

        @GetMapping("/locked/count")
        @PreAuthorize("hasAuthority('setting.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByLocked(
                        @RequestParam Boolean locked) {
                return ResponseEntity.ok(
                                settingService.countByLocked(locked));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('setting.create') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> create(
                        @Valid @RequestBody SettingRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(settingService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('setting.update') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> update(
                        @PathVariable @Positive Long id,
                        @Valid @RequestBody SettingRequest request) {
                return ResponseEntity.ok(
                                settingService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('setting.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable @Positive Long id) {
                settingService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{id}/lock")
        @PreAuthorize("hasAuthority('setting.update') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> lock(
                        @PathVariable @Positive Long id) {
                return ResponseEntity.ok(
                                settingService.lock(id));
        }

        @PutMapping("/{id}/unlock")
        @PreAuthorize("hasAuthority('setting.update') or hasRole('ADMIN')")
        public ResponseEntity<SettingResponse> unlock(
                        @PathVariable @Positive Long id) {
                return ResponseEntity.ok(
                                settingService.unlock(id));
        }
}