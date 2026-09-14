package com.projectmanagement.app.board;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByProjectIdOrderByDisplayOrderAscIdAsc(Long projectId);

    Optional<BoardColumn> findByProjectIdAndStatusId(Long projectId, Long statusId);

    void deleteByProjectId(Long projectId);
}
