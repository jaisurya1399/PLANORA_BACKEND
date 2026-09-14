package com.projectmanagement.app.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectWorkingHoursRepository extends JpaRepository<ProjectWorkingHours, Long> {

    List<ProjectWorkingHours> findByProjectIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            Long projectId, LocalDate date);

    List<ProjectWorkingHours> findByProjectIdOrderByEffectiveFromAsc(Long projectId);

    Optional<ProjectWorkingHours> findByProjectIdAndEffectiveFrom(Long projectId, LocalDate effectiveFrom);
}
