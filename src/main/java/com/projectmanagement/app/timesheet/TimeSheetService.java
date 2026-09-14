package com.projectmanagement.app.timesheet;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSheetService {

        private final TimeSheetRepository timeSheetRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final ProjectAccessService access;
        private final CurrentUserService currentUserService;

        @Transactional(readOnly = true)
        public List<TimeSheetResponse> getAll() {

                return timeSheetRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TimeSheetResponse> getActive() {

                return timeSheetRepository.findByDeletedAtIsNull()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TimeSheetResponse getById(Long id) {

                TimeSheet timeSheet = timeSheetRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Time sheet not found with id: " + id));

                return mapToResponse(timeSheet);
        }

        @Transactional(readOnly = true)
        public List<TimeSheetResponse> getByUser(Long userId) {

                validateUser(userId);

                return timeSheetRepository
                                .findByUserIdAndDeletedAtIsNull(userId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TimeSheetResponse> getByProject(Long projectId) {

                validateProject(projectId);

                return timeSheetRepository
                                .findByProjectIdAndDeletedAtIsNull(projectId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TimeSheetResponse> getByUserAndProject(
                        Long userId,
                        Long projectId) {

                validateUser(userId);
                validateProject(projectId);

                return timeSheetRepository
                                .findByUserIdAndProjectIdAndDeletedAtIsNull(
                                                userId,
                                                projectId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TimeSheetResponse> searchByTask(String task) {

                return timeSheetRepository
                                .findByTaskContainingIgnoreCase(task)
                                .stream()
                                .filter(timeSheet -> timeSheet.getDeletedAt() == null)
                                .map(this::mapToResponse)
                                .toList();
        }

        public TimeSheetResponse create(TimeSheetRequest request) {

                User user = userRepository
                                .findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: "
                                                                + request.getUserId()));

                Project project = null;

                if (request.getProjectId() != null) {

                        project = projectRepository
                                        .findById(request.getProjectId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Project not found with id: "
                                                                        + request.getProjectId()));
                }

                if (project != null) {
                        access.requireEditor(project);
                } else if (!currentUserService.getCurrentUserId().equals(user.getId())) {
                        throw new RuntimeException("You may only create your own unscoped timesheet");
                }

                TimeSheet timeSheet = TimeSheet.builder()
                                .user(user)
                                .project(project)
                                .task(request.getTask())
                                .build();

                return mapToResponse(
                                timeSheetRepository.save(timeSheet));
        }

        public TimeSheetResponse update(
                        Long id,
                        TimeSheetRequest request) {

                TimeSheet timeSheet = timeSheetRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Time sheet not found with id: " + id));

                User user = userRepository
                                .findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: "
                                                                + request.getUserId()));

                Project project = null;

                if (request.getProjectId() != null) {

                        project = projectRepository
                                        .findById(request.getProjectId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Project not found with id: "
                                                                        + request.getProjectId()));
                }

                if (timeSheet.getProject() != null)
                        access.requireEditor(timeSheet.getProject());
                if (project != null)
                        access.requireEditor(project);
                if (!currentUserService.getCurrentUserId().equals(user.getId()) && !access.canManage(project))
                        throw new RuntimeException("You may only edit your own timesheet");

                timeSheet.setUser(user);
                timeSheet.setProject(project);
                timeSheet.setTask(request.getTask());

                return mapToResponse(
                                timeSheetRepository.save(timeSheet));
        }

        public void softDelete(Long id) {

                TimeSheet timeSheet = timeSheetRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Time sheet not found with id: " + id));

                if (timeSheet.getProject() != null)
                        access.requireEditor(timeSheet.getProject());
                else if (!currentUserService.getCurrentUserId().equals(timeSheet.getUser().getId()))
                        throw new RuntimeException("You may only delete your own timesheet");
                timeSheet.setDeletedAt(
                                java.time.LocalDateTime.now());

                timeSheetRepository.save(timeSheet);
        }

        public void restore(Long id) {

                TimeSheet timeSheet = timeSheetRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Time sheet not found with id: " + id));

                timeSheet.setDeletedAt(null);

                timeSheetRepository.save(timeSheet);
        }

        public void permanentDelete(Long id) {

                if (!timeSheetRepository.existsById(id)) {
                        throw new RuntimeException(
                                        "Time sheet not found with id: " + id);
                }

                timeSheetRepository.deleteById(id);
        }

        public void deleteByUser(Long userId) {

                validateUser(userId);

                timeSheetRepository.deleteByUserId(userId);
        }

        public void deleteByProject(Long projectId) {

                validateProject(projectId);

                timeSheetRepository.deleteByProjectId(projectId);
        }

        @Transactional(readOnly = true)
        public long countByUser(Long userId) {

                validateUser(userId);

                return timeSheetRepository.countByUserId(userId);
        }

        @Transactional(readOnly = true)
        public long countByProject(Long projectId) {

                validateProject(projectId);

                return timeSheetRepository.countByProjectId(projectId);
        }

        @Transactional(readOnly = true)
        public long countByUserAndProject(
                        Long userId,
                        Long projectId) {

                validateUser(userId);
                validateProject(projectId);

                return timeSheetRepository
                                .countByUserIdAndProjectId(userId, projectId);
        }

        private void validateUser(Long userId) {

                if (!userRepository.existsById(userId)) {
                        throw new RuntimeException(
                                        "User not found with id: " + userId);
                }
        }

        private void validateProject(Long projectId) {

                if (!projectRepository.existsById(projectId)) {
                        throw new RuntimeException(
                                        "Project not found with id: " + projectId);
                }
        }

        private TimeSheetResponse mapToResponse(
                        TimeSheet timeSheet) {

                User user = timeSheet.getUser();
                Project project = timeSheet.getProject();

                TimeSheetResponse.TimeSheetResponseBuilder builder = TimeSheetResponse.builder()
                                .id(timeSheet.getId())

                                .userId(user.getId())
                                .userName(user.getName())
                                .userEmail(user.getEmail())

                                .task(timeSheet.getTask())

                                .createdAt(timeSheet.getCreatedAt())
                                .updatedAt(timeSheet.getUpdatedAt())
                                .deletedAt(timeSheet.getDeletedAt());

                if (project != null) {
                        builder
                                        .projectId(project.getId())
                                        .projectName(project.getName());
                }

                return builder.build();
        }
}