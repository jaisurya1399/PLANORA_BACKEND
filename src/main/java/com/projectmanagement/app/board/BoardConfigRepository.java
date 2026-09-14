package com.projectmanagement.app.board;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardConfigRepository extends JpaRepository<BoardConfig, Long> {
    Optional<BoardConfig> findByProjectId(Long projectId);
}
