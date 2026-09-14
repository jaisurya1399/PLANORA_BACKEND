package com.projectmanagement.app.timesheet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSheetCellRepository
                extends JpaRepository<TimeSheetCell, Long> {

        List<TimeSheetCell> findByTimeSheetId(Long timeSheetId);

        List<TimeSheetCell> findByTimeSheetIdOrderByDateAsc(Long timeSheetId);

        List<TimeSheetCell> findByTimeSheetIdOrderByDateDesc(Long timeSheetId);

        Optional<TimeSheetCell> findByTimeSheetIdAndDate(
                        Long timeSheetId,
                        LocalDate date);

        List<TimeSheetCell> findByDate(LocalDate date);

        List<TimeSheetCell> findByIsTripTrue();

        List<TimeSheetCell> findByIsTripFalse();

        long countByTimeSheetId(Long timeSheetId);

        long countByTimeSheetIdAndIsTripTrue(Long timeSheetId);

        void deleteByTimeSheetId(Long timeSheetId);
}