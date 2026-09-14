package com.projectmanagement.app.userrole;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    Optional<UserRole> findByUserIdAndRoleId(
            Long userId,
            Long roleId);

    boolean existsByUserIdAndRoleId(
            Long userId,
            Long roleId);

    long countByUserId(Long userId);

    long countByRoleId(Long roleId);

    void deleteByUserId(Long userId);

    void deleteByRoleId(Long roleId);

    @Query("""
            SELECT DISTINCT ur
            FROM UserRole ur
            JOIN FETCH ur.role r
            LEFT JOIN FETCH r.rolePermissions rp
            LEFT JOIN FETCH rp.permission p
            WHERE ur.user.id = :userId
            """)
    List<UserRole> findUserRolesWithPermissions(
            @Param("userId") Long userId);
}