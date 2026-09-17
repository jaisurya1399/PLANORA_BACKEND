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

import com.projectmanagement.app.project.ProjectUser;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.rolepermission.RolePermission;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;
import com.projectmanagement.app.userrole.UserRole;
import com.projectmanagement.app.userrole.UserRoleRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final ProjectUserRepository projectUserRepository;

        public CustomUserDetailsService(
                        UserRepository userRepository,
                        UserRoleRepository userRoleRepository,
                        ProjectUserRepository projectUserRepository) {

                this.userRepository = userRepository;
                this.userRoleRepository = userRoleRepository;
                this.projectUserRepository = projectUserRepository;
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
                // GLOBAL role: only ADMIN is a real system role.
                // Every non-admin user is MEMBER. Project roles are evaluated
                // from project_users and must never become global roles.
                // ------------------------------------------------------------
                List<UserRole> userRoles = userRoleRepository.findUserRolesWithPermissions(user.getId());
                boolean systemAdmin = false;

                for (UserRole userRole : userRoles) {
                        if (userRole == null || userRole.getRole() == null)
                                continue;
                        String roleName = userRole.getRole().getName();
                        if (roleName != null && "ADMIN".equalsIgnoreCase(roleName.trim())) {
                                systemAdmin = true;
                                break;
                        }
                }

                if (systemAdmin) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        addGlobalRolePermissions(authorities, userRoles, "ADMIN");
                } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
                }

                // ------------------------------------------------------------
                // PROJECT permissions: expose the UNION only as Spring
                // authorities so method security can authenticate MEMBER users.
                // Actual project access is still enforced in ProjectAccessService
                // using the requested project's project_users membership.
                // ------------------------------------------------------------
                if (!systemAdmin) {
                        projectUserRepository.findByUserId(user.getId()).stream()
                                        .map(ProjectUser::getRole)
                                        .map(this::projectRolePermissions)
                                        .flatMap(Set::stream)
                                        .filter(p -> p != null && !p.isBlank())
                                        .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                }

                return buildUserDetails(user, authorities);
        }

        private void addGlobalRolePermissions(Set<GrantedAuthority> authorities, List<UserRole> userRoles,
                        String roleName) {
                for (UserRole userRole : userRoles) {
                        if (userRole == null || userRole.getRole() == null)
                                continue;
                        if (!roleName.equalsIgnoreCase(String.valueOf(userRole.getRole().getName())))
                                continue;
                        Set<RolePermission> rolePermissions = userRole.getRole().getRolePermissions();
                        if (rolePermissions == null)
                                continue;
                        for (RolePermission rp : rolePermissions) {
                                if (rp != null && rp.getPermission() != null && rp.getPermission().getName() != null) {
                                        String permission = rp.getPermission().getName().trim();
                                        if (!permission.isBlank())
                                                authorities.add(new SimpleGrantedAuthority(permission));
                                }
                        }
                }
        }

        private Set<String> projectRolePermissions(String role) {
                return com.projectmanagement.app.project.ProjectPermissionCatalog.authoritiesForRole(role);
        }

        private UserDetails buildUserDetails(User user, Set<GrantedAuthority> authorities) {
                return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword() != null ? user.getPassword() : "")
                                .authorities(authorities)
                                .accountExpired(false)
                                .accountLocked(false)
                                .credentialsExpired(false)
                                .disabled(false)
                                .build();
        }

}
