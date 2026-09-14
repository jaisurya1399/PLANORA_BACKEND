package com.projectmanagement.app.role;

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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

        private final RoleService roleService;

        @GetMapping
        @PreAuthorize("hasAuthority('role.view') or hasRole('ADMIN')")
        public ResponseEntity<List<RoleResponse>> getAll() {
                return ResponseEntity.ok(
                                roleService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('role.view') or hasRole('ADMIN')")
        public ResponseEntity<RoleResponse> getById(
                        @PathVariable Long id) {
                return ResponseEntity.ok(
                                roleService.getById(id));
        }

        @GetMapping("/name/{name}")
        @PreAuthorize("hasAuthority('role.view') or hasRole('ADMIN')")
        public ResponseEntity<RoleResponse> getByName(
                        @PathVariable String name) {
                return ResponseEntity.ok(
                                roleService.getByName(name));
        }

        @GetMapping("/guard/{guardName}")
        @PreAuthorize("hasAuthority('role.view') or hasRole('ADMIN')")
        public ResponseEntity<List<RoleResponse>> getByGuardName(
                        @PathVariable String guardName) {
                return ResponseEntity.ok(
                                roleService.getByGuardName(guardName));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('role.create') or hasRole('ADMIN')")
        public ResponseEntity<RoleResponse> create(
                        @Valid @RequestBody RoleRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(roleService.create(request));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('role.update') or hasRole('ADMIN')")
        public ResponseEntity<RoleResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody RoleRequest request) {
                return ResponseEntity.ok(
                                roleService.update(id, request));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('role.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {
                roleService.delete(id);

                return ResponseEntity.noContent().build();
        }
}