package com.projectmanagement.app.permission;

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

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

        private final PermissionService permissionService;

        public PermissionController(PermissionService permissionService) {
                this.permissionService = permissionService;
        }

        // =========================
        // GET ALL PERMISSIONS
        // =========================
        @GetMapping
        @PreAuthorize("hasAuthority('permission.view') or hasRole('ADMIN')")
        public ResponseEntity<List<Permission>> getAllPermissions() {

                return ResponseEntity.ok(
                                permissionService.getAllPermissions());
        }

        // =========================
        // GET PERMISSION BY ID
        // =========================
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('permission.view') or hasRole('ADMIN')")
        public ResponseEntity<Permission> getPermissionById(
                        @PathVariable @Positive Long id) {

                return ResponseEntity.ok(
                                permissionService.getPermissionById(id));
        }

        // =========================
        // GET PERMISSION BY NAME
        // =========================
        @GetMapping("/name/{name}")
        @PreAuthorize("hasAuthority('permission.view') or hasRole('ADMIN')")
        public ResponseEntity<Permission> getPermissionByName(
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                permissionService.getPermissionByName(name));
        }

        // =========================
        // CHECK PERMISSION EXISTS
        // =========================
        @GetMapping("/exists/name/{name}")
        @PreAuthorize("hasAuthority('permission.view') or hasRole('ADMIN')")
        public ResponseEntity<Boolean> existsByName(
                        @PathVariable String name) {

                return ResponseEntity.ok(
                                permissionService.existsByName(name));
        }

        // =========================
        // CREATE PERMISSION
        // =========================
        @PostMapping
        @PreAuthorize("hasAuthority('permission.create') or hasRole('ADMIN')")
        public ResponseEntity<Permission> createPermission(
                        @RequestBody Permission request) {

                Permission permission = permissionService.createPermission(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(permission);
        }

        // =========================
        // UPDATE PERMISSION
        // =========================
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('permission.update') or hasRole('ADMIN')")
        public ResponseEntity<Permission> updatePermission(
                        @PathVariable @Positive Long id,
                        @RequestBody Permission request) {

                Permission permission = permissionService.updatePermission(id, request);

                return ResponseEntity.ok(permission);
        }

        // =========================
        // DELETE PERMISSION
        // =========================
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('permission.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deletePermission(
                        @PathVariable @Positive Long id) {

                permissionService.deletePermission(id);

                return ResponseEntity.noContent().build();
        }
}