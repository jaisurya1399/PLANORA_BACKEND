package com.projectmanagement.app.userrole;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

        private final UserRoleService service;

        @GetMapping
        @PreAuthorize("hasAuthority('user_role.view') or hasRole('ADMIN')")
        public ResponseEntity<List<UserRoleResponse>> getAll() {
                return ResponseEntity.ok(service.getAll());
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('user_role.view') or hasRole('ADMIN')")
        public ResponseEntity<List<UserRoleResponse>> getByUser(
                        @PathVariable Long userId) {
                return ResponseEntity.ok(
                                service.getByUser(userId));
        }

        @GetMapping("/role/{roleId}")
        @PreAuthorize("hasAuthority('user_role.view') or hasRole('ADMIN')")
        public ResponseEntity<List<UserRoleResponse>> getByRole(
                        @PathVariable Long roleId) {
                return ResponseEntity.ok(
                                service.getByRole(roleId));
        }

        @GetMapping("/{userId}/{roleId}")
        @PreAuthorize("hasAuthority('user_role.view') or hasRole('ADMIN')")
        public ResponseEntity<UserRoleResponse> getById(
                        @PathVariable Long userId,
                        @PathVariable Long roleId) {
                return ResponseEntity.ok(
                                service.getById(userId, roleId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('user_role.assign') or hasRole('ADMIN')")
        public ResponseEntity<UserRoleResponse> assignRole(
                        @Valid @RequestBody UserRoleRequest request) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(service.assignRole(request));
        }

        @DeleteMapping("/{userId}/{roleId}")
        @PreAuthorize("hasAuthority('user_role.remove') or hasRole('ADMIN')")
        public ResponseEntity<Void> removeRole(
                        @PathVariable Long userId,
                        @PathVariable Long roleId) {
                service.removeRole(userId, roleId);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user/{userId}")
        @PreAuthorize("hasAuthority('user_role.remove') or hasRole('ADMIN')")
        public ResponseEntity<Void> removeAllRoles(
                        @PathVariable Long userId) {
                service.removeAllRoles(userId);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/user/{userId}/count")
        @PreAuthorize("hasAuthority('user_role.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByUser(
                        @PathVariable Long userId) {
                return ResponseEntity.ok(
                                service.countByUser(userId));
        }

        @GetMapping("/role/{roleId}/count")
        @PreAuthorize("hasAuthority('user_role.view') or hasRole('ADMIN')")
        public ResponseEntity<Long> countByRole(
                        @PathVariable Long roleId) {
                return ResponseEntity.ok(
                                service.countByRole(roleId));
        }
}