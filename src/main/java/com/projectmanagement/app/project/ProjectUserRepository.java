package com.projectmanagement.app.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {

        List<ProjectUser> findByProjectId(Long projectId);

        List<ProjectUser> findByUserId(Long userId);

        List<ProjectUser> findByProjectIdAndRole(
                        Long projectId,
                        String role);

        List<ProjectUser> findByProjectIdAndRoleIgnoreCase(
                        Long projectId,
                        String role);

        Optional<ProjectUser> findByProjectIdAndUserId(
                        Long projectId,
                        Long userId);

        Optional<ProjectUser> findByProjectIdAndUserIdAndRole(
                        Long projectId,
                        Long userId,
                        String role);

        boolean existsByProjectIdAndUserId(
                        Long projectId,
                        Long userId);

        boolean existsByProjectIdAndUserIdAndIdNot(
                        Long projectId,
                        Long userId,
                        Long id);

        void deleteByProjectId(Long projectId);

        void deleteByUserId(Long userId);

        long countByProjectId(Long projectId);

        long countByUserId(Long userId);

        // Only users whose deletedAt is NULL
        List<ProjectUser> findByProjectIdAndUser_DeletedAtIsNull(Long projectId);
}