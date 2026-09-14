package com.projectmanagement.app.dailyscrum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyScrumRepository
                extends JpaRepository<DailyScrum, Long> {

        Optional<DailyScrum> findByUserIdAndProjectIdAndScrumDate(
                        Long userId,
                        Long projectId,
                        LocalDate scrumDate);

        List<DailyScrum> findByUserIdOrderByScrumDateDesc(
                        Long userId);

        List<DailyScrum> findByUserIdAndScrumDateBetweenOrderByScrumDateAsc(
                        Long userId,
                        LocalDate startDate,
                        LocalDate endDate);

        List<DailyScrum> findByScrumDateBetweenOrderByScrumDateAsc(
                        LocalDate startDate,
                        LocalDate endDate);

        List<DailyScrum> findByProjectIdAndScrumDateBetweenOrderByScrumDateAsc(
                        Long projectId,
                        LocalDate startDate,
                        LocalDate endDate);

        boolean existsByUserIdAndProjectIdAndScrumDate(
                        Long userId,
                        Long projectId,
                        LocalDate scrumDate);
}