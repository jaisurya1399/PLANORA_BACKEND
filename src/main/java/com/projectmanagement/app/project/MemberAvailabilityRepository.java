package com.projectmanagement.app.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAvailabilityRepository extends JpaRepository<MemberAvailability, Long> {

        List<MemberAvailability> findByProjectIdAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(
                        Long projectId, LocalDate startDate, LocalDate endDate);

        List<MemberAvailability> findByProjectIdAndUserIdAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(
                        Long projectId, Long userId, LocalDate startDate, LocalDate endDate);

        Optional<MemberAvailability> findByIdAndProjectId(Long id, Long projectId);

        boolean existsByProjectIdAndUserIdAndAvailabilityDate(
                        Long projectId, Long userId, LocalDate availabilityDate);

        boolean existsByProjectIdAndUserIdAndAvailabilityDateAndIdNot(
                        Long projectId, Long userId, LocalDate availabilityDate, Long id);
}
