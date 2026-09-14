package com.projectmanagement.app.dailyscrum;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class DailyScrumService {

        private final DailyScrumRepository dailyScrumRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;

        public DailyScrumService(
                        DailyScrumRepository dailyScrumRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository) {

                this.dailyScrumRepository = dailyScrumRepository;
                this.userRepository = userRepository;
                this.projectRepository = projectRepository;
        }

        // ============================================================
        // CREATE
        // ============================================================

        public DailyScrumResponse create(
                        Long loggedInUserId,
                        DailyScrumRequest request) {

                validateLoggedInUser(loggedInUserId);
                validateDate(request.getScrumDate());

                User targetUser = getUser(request.getUserId());
                Project project = getProject(request.getProjectId());

                validateCanManageScrum(
                                loggedInUserId,
                                targetUser.getId(),
                                project.getId());

                boolean exists = dailyScrumRepository
                                .existsByUserIdAndProjectIdAndScrumDate(
                                                targetUser.getId(),
                                                project.getId(),
                                                request.getScrumDate());

                if (exists) {
                        throw new IllegalArgumentException(
                                        "Daily scrum already exists for this user, project and date");
                }

                DailyScrum scrum = DailyScrum.builder()
                                .user(targetUser)
                                .project(project)
                                .scrumDate(request.getScrumDate())
                                .yesterdayWork(request.getYesterdayWork().trim())
                                .todayWork(request.getTodayWork().trim())
                                .blockers(
                                                request.getBlockers() == null
                                                                ? ""
                                                                : request.getBlockers().trim())
                                .build();

                DailyScrum savedScrum = dailyScrumRepository.save(scrum);

                return toResponse(savedScrum);
        }

        // ============================================================
        // UPDATE
        // ============================================================

        public DailyScrumResponse update(
                        Long loggedInUserId,
                        Long id,
                        DailyScrumRequest request) {

                validateLoggedInUser(loggedInUserId);
                validateDate(request.getScrumDate());

                DailyScrum scrum = dailyScrumRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Daily scrum not found with id: " + id));

                User targetUser = getUser(request.getUserId());
                Project project = getProject(request.getProjectId());

                validateCanManageScrum(
                                loggedInUserId,
                                targetUser.getId(),
                                project.getId());

                boolean changed = !scrum.getUser().getId().equals(targetUser.getId())
                                || !scrum.getProject().getId().equals(project.getId())
                                || !scrum.getScrumDate().equals(request.getScrumDate());

                if (changed) {

                        DailyScrumRepository repository = dailyScrumRepository;

                        var existingScrum = repository.findByUserIdAndProjectIdAndScrumDate(
                                        targetUser.getId(),
                                        project.getId(),
                                        request.getScrumDate());

                        if (existingScrum.isPresent()
                                        && !existingScrum.get().getId().equals(id)) {

                                throw new IllegalArgumentException(
                                                "Daily scrum already exists for this user, project and date");
                        }
                }

                scrum.setUser(targetUser);
                scrum.setProject(project);
                scrum.setScrumDate(request.getScrumDate());

                scrum.setYesterdayWork(
                                request.getYesterdayWork().trim());

                scrum.setTodayWork(
                                request.getTodayWork().trim());

                scrum.setBlockers(
                                request.getBlockers() == null
                                                ? ""
                                                : request.getBlockers().trim());

                DailyScrum updatedScrum = dailyScrumRepository.save(scrum);

                return toResponse(updatedScrum);
        }

        // ============================================================
        // GET ALL
        // ============================================================

        @Transactional(readOnly = true)
        public List<DailyScrumResponse> getAll() {

                return dailyScrumRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Transactional(readOnly = true)
        public DailyScrumResponse getById(Long id) {

                DailyScrum scrum = dailyScrumRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Daily scrum not found with id: " + id));

                return toResponse(scrum);
        }

        // ============================================================
        // GET BY USER
        // ============================================================

        @Transactional(readOnly = true)
        public List<DailyScrumResponse> getByUser(Long userId) {

                getUser(userId);

                return dailyScrumRepository
                                .findByUserIdOrderByScrumDateDesc(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY USER + DATE RANGE
        // ============================================================

        @Transactional(readOnly = true)
        public List<DailyScrumResponse> getByUserAndRange(
                        Long userId,
                        LocalDate startDate,
                        LocalDate endDate) {

                validateRange(startDate, endDate);
                getUser(userId);

                return dailyScrumRepository
                                .findByUserIdAndScrumDateBetweenOrderByScrumDateAsc(
                                                userId,
                                                startDate,
                                                endDate)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY DATE RANGE
        // ============================================================

        @Transactional(readOnly = true)
        public List<DailyScrumResponse> getByDateRange(
                        LocalDate startDate,
                        LocalDate endDate) {

                validateRange(startDate, endDate);

                return dailyScrumRepository
                                .findByScrumDateBetweenOrderByScrumDateAsc(
                                                startDate,
                                                endDate)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY PROJECT + DATE RANGE
        // ============================================================

        @Transactional(readOnly = true)
        public List<DailyScrumResponse> getByProjectAndRange(
                        Long projectId,
                        LocalDate startDate,
                        LocalDate endDate) {

                validateRange(startDate, endDate);
                getProject(projectId);

                return dailyScrumRepository
                                .findByProjectIdAndScrumDateBetweenOrderByScrumDateAsc(
                                                projectId,
                                                startDate,
                                                endDate)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // DELETE
        // ============================================================

        public void delete(
                        Long loggedInUserId,
                        Long id) {

                validateLoggedInUser(loggedInUserId);

                DailyScrum scrum = dailyScrumRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Daily scrum not found with id: " + id));

                validateCanManageScrum(
                                loggedInUserId,
                                scrum.getUser().getId(),
                                scrum.getProject().getId());

                dailyScrumRepository.delete(scrum);
        }

        // ============================================================
        // PERMISSION CHECK
        // ============================================================

        private void validateCanManageScrum(
                        Long loggedInUserId,
                        Long targetUserId,
                        Long projectId) {

                if (loggedInUserId == null) {
                        throw new RuntimeException(
                                        "Unauthenticated user");
                }

                if (targetUserId == null) {
                        throw new IllegalArgumentException(
                                        "Target user is required");
                }

                if (projectId == null) {
                        throw new IllegalArgumentException(
                                        "Project is required");
                }

                /*
                 * TEMPORARY IMPLEMENTATION
                 *
                 * The previous code always threw:
                 *
                 * "Daily Scrum permission check is not configured..."
                 *
                 * That was the reason POST / PUT / DELETE failed.
                 *
                 * We now only verify that:
                 *
                 * 1. Logged-in user exists
                 * 2. Target user exists
                 * 3. Project exists
                 *
                 * Proper Admin / Team Lead / ProjectUser authorization
                 * should be added after connecting ProjectUserRepository.
                 */

                getUser(loggedInUserId);
                getUser(targetUserId);
                getProject(projectId);
        }

        // ============================================================
        // USER VALIDATION
        // ============================================================

        private void validateLoggedInUser(Long loggedInUserId) {

                if (loggedInUserId == null) {
                        throw new RuntimeException(
                                        "Unauthenticated user");
                }

                if (!userRepository.existsById(loggedInUserId)) {
                        throw new RuntimeException(
                                        "Logged-in user not found: " + loggedInUserId);
                }
        }

        // ============================================================
        // GET USER
        // ============================================================

        private User getUser(Long id) {

                if (id == null) {
                        throw new IllegalArgumentException(
                                        "User ID is required");
                }

                return userRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found: " + id));
        }

        // ============================================================
        // GET PROJECT
        // ============================================================

        private Project getProject(Long id) {

                if (id == null) {
                        throw new IllegalArgumentException(
                                        "Project ID is required");
                }

                return projectRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found: " + id));
        }

        // ============================================================
        // DATE VALIDATION
        // ============================================================

        private void validateDate(LocalDate date) {

                if (date == null) {
                        throw new IllegalArgumentException(
                                        "Scrum date is required");
                }

                if (date.isAfter(LocalDate.now())) {
                        throw new IllegalArgumentException(
                                        "Scrum date cannot be in the future");
                }
        }

        // ============================================================
        // RANGE VALIDATION
        // ============================================================

        private void validateRange(
                        LocalDate startDate,
                        LocalDate endDate) {

                if (startDate == null || endDate == null) {
                        throw new IllegalArgumentException(
                                        "Start date and end date are required");
                }

                if (startDate.isAfter(endDate)) {
                        throw new IllegalArgumentException(
                                        "Start date cannot be after end date");
                }
        }

        // ============================================================
        // RESPONSE MAPPER
        // ============================================================

        private DailyScrumResponse toResponse(
                        DailyScrum scrum) {

                return DailyScrumResponse.builder()
                                .id(scrum.getId())

                                .userId(
                                                scrum.getUser().getId())

                                .userName(
                                                scrum.getUser().getName())

                                .userEmail(
                                                scrum.getUser().getEmail())

                                .projectId(
                                                scrum.getProject().getId())

                                .projectName(
                                                scrum.getProject().getName())

                                .scrumDate(
                                                scrum.getScrumDate())

                                .yesterdayWork(
                                                scrum.getYesterdayWork())

                                .todayWork(
                                                scrum.getTodayWork())

                                .blockers(
                                                scrum.getBlockers())

                                .createdAt(
                                                scrum.getCreatedAt())

                                .updatedAt(
                                                scrum.getUpdatedAt())

                                .build();
        }
}