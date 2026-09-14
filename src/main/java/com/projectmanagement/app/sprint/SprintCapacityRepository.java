package com.projectmanagement.app.sprint;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintCapacityRepository extends JpaRepository<SprintCapacity, Long> {

    List<SprintCapacity> findBySprintIdOrderByUser_NameAsc(Long sprintId);

    Optional<SprintCapacity> findBySprintIdAndUserId(Long sprintId, Long userId);

    void deleteBySprintId(Long sprintId);
}