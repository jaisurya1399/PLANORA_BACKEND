package com.projectmanagement.app.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectFavoriteRepository
                extends JpaRepository<ProjectFavorite, Long> {

        List<ProjectFavorite> findByUserId(Long userId);

        List<ProjectFavorite> findByProjectId(Long projectId);

        Optional<ProjectFavorite> findByUserIdAndProjectId(
                        Long userId,
                        Long projectId);

        boolean existsByUserIdAndProjectId(
                        Long userId,
                        Long projectId);

        long countByUserId(Long userId);

        long countByProjectId(Long projectId);

        void deleteByUserId(Long userId);

        void deleteByProjectId(Long projectId);
}