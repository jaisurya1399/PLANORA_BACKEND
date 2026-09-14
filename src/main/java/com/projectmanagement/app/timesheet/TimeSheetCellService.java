package com.projectmanagement.app.timesheet;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSheetCellService {

        private final TimeSheetCellRepository timeSheetCellRepository;
        private final TimeSheetRepository timeSheetRepository;

        @Transactional(readOnly = true)
        public List<TimeSheetCellResponse> getAll() {
                return timeSheetCellRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TimeSheetCellResponse getById(Long id) {
                TimeSheetCell cell = timeSheetCellRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Time sheet cell not found"));

                return toResponse(cell);
        }

        @Transactional(readOnly = true)
        public List<TimeSheetCellResponse> getByTimeSheet(Long timeSheetId) {
                validateTimeSheetExists(timeSheetId);

                return timeSheetCellRepository
                                .findByTimeSheetIdOrderByDateAsc(timeSheetId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TimeSheetCellResponse> getByTimeSheetDesc(Long timeSheetId) {
                validateTimeSheetExists(timeSheetId);

                return timeSheetCellRepository
                                .findByTimeSheetIdOrderByDateDesc(timeSheetId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TimeSheetCellResponse getByTimeSheetAndDate(
                        Long timeSheetId,
                        LocalDate date) {
                validateTimeSheetExists(timeSheetId);

                TimeSheetCell cell = timeSheetCellRepository
                                .findByTimeSheetIdAndDate(timeSheetId, date)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Time sheet cell not found for the specified date"));

                return toResponse(cell);
        }

        @Transactional(readOnly = true)
        public List<TimeSheetCellResponse> getByDate(LocalDate date) {
                return timeSheetCellRepository.findByDate(date)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TimeSheetCellResponse> getTripCells() {
                return timeSheetCellRepository.findByIsTripTrue()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public long countByTimeSheet(Long timeSheetId) {
                validateTimeSheetExists(timeSheetId);
                return timeSheetCellRepository.countByTimeSheetId(timeSheetId);
        }

        @Transactional(readOnly = true)
        public long countTripCells(Long timeSheetId) {
                validateTimeSheetExists(timeSheetId);
                return timeSheetCellRepository.countByTimeSheetIdAndIsTripTrue(timeSheetId);
        }

        public TimeSheetCellResponse create(TimeSheetCellRequest request) {

                TimeSheet timeSheet = getTimeSheet(request.getTimeSheetId());

                TimeSheetCell cell = TimeSheetCell.builder()
                                .timeSheet(timeSheet)
                                .value(request.getValue())
                                .isTrip(
                                                request.getIsTrip() != null
                                                                ? request.getIsTrip()
                                                                : false)
                                .comment(request.getComment())
                                .date(request.getDate())
                                .build();

                return toResponse(timeSheetCellRepository.save(cell));
        }

        public TimeSheetCellResponse update(
                        Long id,
                        TimeSheetCellRequest request) {
                TimeSheetCell cell = timeSheetCellRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Time sheet cell not found"));

                TimeSheet timeSheet = getTimeSheet(request.getTimeSheetId());

                cell.setTimeSheet(timeSheet);
                cell.setValue(request.getValue());
                cell.setIsTrip(
                                request.getIsTrip() != null
                                                ? request.getIsTrip()
                                                : false);
                cell.setComment(request.getComment());
                cell.setDate(request.getDate());

                return toResponse(timeSheetCellRepository.save(cell));
        }

        public void delete(Long id) {
                if (!timeSheetCellRepository.existsById(id)) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Time sheet cell not found");
                }

                timeSheetCellRepository.deleteById(id);
        }

        public void deleteByTimeSheet(Long timeSheetId) {
                validateTimeSheetExists(timeSheetId);
                timeSheetCellRepository.deleteByTimeSheetId(timeSheetId);
        }

        private TimeSheet getTimeSheet(Long id) {
                return timeSheetRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Time sheet not found"));
        }

        private void validateTimeSheetExists(Long id) {
                if (!timeSheetRepository.existsById(id)) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Time sheet not found");
                }
        }

        private TimeSheetCellResponse toResponse(TimeSheetCell cell) {

                TimeSheet timeSheet = cell.getTimeSheet();

                Long userId = null;
                Long projectId = null;
                String task = null;

                if (timeSheet != null) {

                        task = timeSheet.getTask();

                        if (timeSheet.getUser() != null) {
                                userId = timeSheet.getUser().getId();
                        }

                        if (timeSheet.getProject() != null) {
                                projectId = timeSheet.getProject().getId();
                        }
                }

                return TimeSheetCellResponse.builder()
                                .id(cell.getId())
                                .timeSheetId(
                                                timeSheet != null
                                                                ? timeSheet.getId()
                                                                : null)
                                .userId(userId)
                                .projectId(projectId)
                                .task(task)
                                .value(cell.getValue())
                                .isTrip(cell.getIsTrip())
                                .comment(cell.getComment())
                                .date(cell.getDate())
                                .createdAt(cell.getCreatedAt())
                                .updatedAt(cell.getUpdatedAt())
                                .build();
        }
}