package com.projectmanagement.app.auth;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.role.Role;
import com.projectmanagement.app.rolepermission.RolePermission;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;
import com.projectmanagement.app.userrole.UserRole;
import com.projectmanagement.app.userrole.UserRoleRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;

        public CustomUserDetailsService(
                        UserRepository userRepository,
                        UserRoleRepository userRoleRepository) {

                this.userRepository = userRepository;
                this.userRoleRepository = userRoleRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String email)
                        throws UsernameNotFoundException {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + email));

                // ------------------------------------------------------------
                // Check soft-deleted user
                // ------------------------------------------------------------

                if (user.getDeletedAt() != null || user.getPassword() == null || user.getPassword().isBlank()) {
                        throw new UsernameNotFoundException(
                                        "User account is unavailable for password authentication");
                }

                Set<GrantedAuthority> authorities = new HashSet<>();

                // ------------------------------------------------------------
                // Load user roles with permissions
                // ------------------------------------------------------------

                List<UserRole> userRoles = userRoleRepository.findUserRolesWithPermissions(
                                user.getId());

                for (UserRole userRole : userRoles) {

                        if (userRole == null) {
                                continue;
                        }

                        Role role = userRole.getRole();

                        if (role == null) {
                                continue;
                        }

                        // --------------------------------------------------------
                        // Add ROLE_xxx authority
                        // Example: ADMIN -> ROLE_ADMIN
                        // --------------------------------------------------------

                        String roleName = role.getName();

                        if (roleName != null && !roleName.isBlank()) {

                                authorities.add(
                                                new SimpleGrantedAuthority(
                                                                "ROLE_" +
                                                                                roleName.trim().toUpperCase()));
                        }

                        // --------------------------------------------------------
                        // Add permissions
                        // --------------------------------------------------------

                        Set<RolePermission> rolePermissions = role.getRolePermissions();

                        if (rolePermissions == null ||
                                        rolePermissions.isEmpty()) {
                                continue;
                        }

                        for (RolePermission rolePermission : rolePermissions) {

                                if (rolePermission == null) {
                                        continue;
                                }

                                if (rolePermission.getPermission() == null) {
                                        continue;
                                }

                                String permissionName = rolePermission
                                                .getPermission()
                                                .getName();

                                if (permissionName == null ||
                                                permissionName.isBlank()) {
                                        continue;
                                }

                                authorities.add(
                                                new SimpleGrantedAuthority(
                                                                permissionName.trim()));
                        }
                }

                // ------------------------------------------------------------
                // Fallback role
                // Only add USER role when user has no role at all
                // ------------------------------------------------------------

                boolean hasRole = authorities.stream()
                                .anyMatch(authority -> authority.getAuthority()
                                                .startsWith("ROLE_"));

                if (!hasRole) {
                        authorities.add(
                                        new SimpleGrantedAuthority("ROLE_USER"));
                }

                // ------------------------------------------------------------
                // Build Spring Security UserDetails
                // ------------------------------------------------------------

                return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(
                                                user.getPassword() != null
                                                                ? user.getPassword()
                                                                : "")
                                .authorities(authorities)
                                .accountExpired(false)
                                .accountLocked(false)
                                .credentialsExpired(false)
                                .disabled(false)
                                .build();
        }
}