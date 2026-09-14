package com.projectmanagement.app.sprint;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository
                extends JpaRepository<Sprint, Long> {

        List<Sprint> findByProjectIdOrderByCreatedAtDesc(
                        Long projectId);

        List<Sprint> findByProjectIdAndStatus(
                        Long projectId,
                        SprintStatus status);

        Optional<Sprint> findByIdAndProjectId(
                        Long id,
                        Long projectId);

        boolean existsByProjectIdAndName(
                        Long projectId,
                        String name);

        boolean existsByProjectIdAndNameAndIdNot(
                        Long projectId,
                        String name,
                        Long id);

        boolean existsByProjectIdAndStatus(
                        Long projectId,
                        SprintStatus status);
}