package com.projectmanagement.app.timesheet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSheetRepository
                extends JpaRepository<TimeSheet, Long> {

        List<TimeSheet> findByDeletedAtIsNull();

        List<TimeSheet> findByUserId(Long userId);

        List<TimeSheet> findByUserIdAndDeletedAtIsNull(Long userId);

        List<TimeSheet> findByProjectId(Long projectId);

        List<TimeSheet> findByProjectIdAndDeletedAtIsNull(Long projectId);

        List<TimeSheet> findByUserIdAndProjectId(
                        Long userId,
                        Long projectId);

        List<TimeSheet> findByUserIdAndProjectIdAndDeletedAtIsNull(
                        Long userId,
                        Long projectId);

        List<TimeSheet> findByTaskContainingIgnoreCase(String task);

        long countByUserId(Long userId);

        long countByProjectId(Long projectId);

        long countByUserIdAndProjectId(
                        Long userId,
                        Long projectId);

        void deleteByUserId(Long userId);

        void deleteByProjectId(Long projectId);
}