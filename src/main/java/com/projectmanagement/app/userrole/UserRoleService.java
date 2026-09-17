package com.projectmanagement.app.userrole;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.role.Role;
import com.projectmanagement.app.role.RoleRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

        private final UserRoleRepository repository;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        @Transactional(readOnly = true)
        public List<UserRoleResponse> getAll() {
                return repository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public UserRoleResponse getById(
                        Long userId,
                        Long roleId) {
                UserRole userRole = repository
                                .findByUserIdAndRoleId(userId, roleId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User role assignment not found"));

                return toResponse(userRole);
        }

        @Transactional(readOnly = true)
        public List<UserRoleResponse> getByUser(Long userId) {

                if (!userRepository.existsById(userId)) {
                        throw new RuntimeException(
                                        "User not found: " + userId);
                }

                return repository.findByUserId(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<UserRoleResponse> getByRole(Long roleId) {

                if (!roleRepository.existsById(roleId)) {
                        throw new RuntimeException(
                                        "Role not found: " + roleId);
                }

                return repository.findByRoleId(roleId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        public UserRoleResponse assignRole(
                        UserRoleRequest request) {

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found: "
                                                                + request.getUserId()));

                Role role = roleRepository.findById(request.getRoleId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Role not found: "
                                                                + request.getRoleId()));

                // Global user-role assignments are intentionally limited to ADMIN.
                // Normal users are global MEMBERs; PROJECT_ADMIN/TEAM_LEAD/DEVELOPER/
                // VIEWER belong exclusively in project_users.
                if (!"ADMIN".equalsIgnoreCase(role.getName())) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Only the ADMIN global role can be assigned at system scope; project roles belong to project membership");
                }

                if ("ADMIN".equalsIgnoreCase(role.getName()) && repository.countByRoleId(role.getId()) >= 1) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Exactly one System Admin is allowed");
                }

                if (repository.existsByUserIdAndRoleId(
                                request.getUserId(),
                                request.getRoleId())) {
                        throw new RuntimeException(
                                        "Role is already assigned to this user");
                }

                UserRoleId id = new UserRoleId(
                                role.getId(),
                                user.getId(),
                                UserRole.USER_MODEL_TYPE);

                UserRole userRole = UserRole.builder()
                                .id(id)
                                .user(user)
                                .role(role)
                                .modelType(UserRole.USER_MODEL_TYPE)
                                .build();

                return toResponse(repository.save(userRole));
        }

        public void removeRole(
                        Long userId,
                        Long roleId) {

                UserRole userRole = repository
                                .findByUserIdAndRoleId(userId, roleId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User role assignment not found"));

                if ("ADMIN".equalsIgnoreCase(userRole.getRole().getName())
                                && repository.countByRoleId(roleId) <= 1) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "The last System Admin cannot be removed");
                }

                repository.delete(userRole);
        }

        public void removeAllRoles(Long userId) {

                if (!userRepository.existsById(userId)) {
                        throw new RuntimeException(
                                        "User not found: " + userId);
                }

                List<UserRole> assignments = repository.findByUserId(userId);
                boolean isSystemAdmin = assignments.stream()
                                .anyMatch(ur -> ur.getRole() != null
                                                && "ADMIN".equalsIgnoreCase(ur.getRole().getName()));
                if (isSystemAdmin) {
                        long adminCount = assignments.stream()
                                        .filter(ur -> ur.getRole() != null
                                                        && "ADMIN".equalsIgnoreCase(ur.getRole().getName()))
                                        .count();
                        if (adminCount > 0 && repository.countByRoleId(assignments.stream()
                                        .filter(ur -> ur.getRole() != null
                                                        && "ADMIN".equalsIgnoreCase(ur.getRole().getName()))
                                        .findFirst().get().getRole().getId()) <= 1) {
                                throw new org.springframework.security.access.AccessDeniedException(
                                                "The last System Admin cannot have all roles removed");
                        }
                }
                repository.deleteByUserId(userId);
        }

        @Transactional(readOnly = true)
        public long countByUser(Long userId) {

                if (!userRepository.existsById(userId)) {
                        throw new RuntimeException(
                                        "User not found: " + userId);
                }

                return repository.countByUserId(userId);
        }

        @Transactional(readOnly = true)
        public long countByRole(Long roleId) {

                if (!roleRepository.existsById(roleId)) {
                        throw new RuntimeException(
                                        "Role not found: " + roleId);
                }

                return repository.countByRoleId(roleId);
        }

        private UserRoleResponse toResponse(UserRole userRole) {

                User user = userRole.getUser();
                Role role = userRole.getRole();

                return UserRoleResponse.builder()
                                .userId(user.getId())
                                .userName(user.getName())
                                .userEmail(user.getEmail())
                                .roleId(role.getId())
                                .roleName(role.getName())
                                .guardName(role.getGuardName())
                                .modelType(userRole.getModelType())
                                .build();
        }
}