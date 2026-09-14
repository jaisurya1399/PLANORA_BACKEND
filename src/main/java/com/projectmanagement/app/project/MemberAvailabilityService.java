package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class MemberAvailabilityService {

    private final MemberAvailabilityRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserService currentUserService;
    private final ProjectWorkingHoursService projectWorkingHoursService;

    public MemberAvailabilityService(
            MemberAvailabilityRepository repository,
            ProjectRepository projectRepository,
            ProjectUserRepository projectUserRepository,
            UserRepository userRepository,
            ProjectAccessService projectAccessService,
            CurrentUserService currentUserService,
            ProjectWorkingHoursService projectWorkingHoursService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.userRepository = userRepository;
        this.projectAccessService = projectAccessService;
        this.currentUserService = currentUserService;
        this.projectWorkingHoursService = projectWorkingHoursService;
    }

    @Transactional(readOnly = true)
    public List<MemberAvailabilityResponse> getProjectAvailability(
            Long projectId, LocalDate startDate, LocalDate endDate) {

        validateRange(startDate, endDate);
        Project project = getProject(projectId);
        projectAccessService.requireView(project);

        Long currentUserId = currentUserService.getCurrentUserId();
        List<MemberAvailability> entries = repository
                .findByProjectIdAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(
                        projectId, startDate, endDate);

        if (isManager(project, currentUserId)) {
            return entries.stream().map(this::toResponse).toList();
        }

        return entries.stream()
                .filter(entry -> entry.getUser() == null
                        || (currentUserId != null
                                && currentUserId.equals(entry.getUser().getId())))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberAvailabilityResponse> getUserAvailability(
            Long projectId, Long userId, LocalDate startDate, LocalDate endDate) {

        validateRange(startDate, endDate);
        Project project = getProject(projectId);
        projectAccessService.requireView(project);
        requireProjectMember(projectId, userId);

        Long currentUserId = currentUserService.getCurrentUserId();
        if (!isManager(project, currentUserId) && !userId.equals(currentUserId)) {
            throw new RuntimeException("You can only view your own availability");
        }

        return repository
                .findByProjectIdAndUserIdAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(
                        projectId, userId, startDate, endDate)
                .stream().map(this::toResponse).toList();
    }

    public MemberAvailabilityResponse create(
            Long projectId, MemberAvailabilityRequest request) {

        Project project = getProject(projectId);
        projectAccessService.requireView(project);
        validateRequest(request);

        Long currentUserId = currentUserService.getCurrentUserId();
        boolean manager = isManager(project, currentUserId);

        User user;
        if (manager) {
            user = resolveUser(projectId, request.getUserId());
        } else {
            if (request.getAvailabilityType() == MemberAvailabilityType.HOLIDAY) {
                throw new RuntimeException("Only project admins can create project-wide holidays");
            }
            user = currentUserService.getCurrentUser();
            requireProjectMember(projectId, user.getId());
            requireSelfUpdateOpen(projectId, user.getId());
            requireNotPast(request.getAvailabilityDate());
        }

        validateScope(request, user);

        if (user != null && repository.existsByProjectIdAndUserIdAndAvailabilityDate(
                projectId, user.getId(), request.getAvailabilityDate())) {
            throw new IllegalArgumentException("Availability already exists for this member and date");
        }

        if (user == null && repository.existsByProjectIdAndUserIdAndAvailabilityDate(
                projectId, null, request.getAvailabilityDate())) {
            throw new IllegalArgumentException("A project-wide holiday already exists for this date");
        }

        MemberAvailability entry = MemberAvailability.builder()
                .project(project)
                .user(user)
                .availabilityDate(request.getAvailabilityDate())
                .availabilityType(request.getAvailabilityType())
                .availableHours(normalizeHours(projectId, request.getAvailabilityDate(), request.getAvailabilityType(),
                        request.getAvailableHours()))
                .reason(normalizeReason(request.getReason()))
                .build();

        return toResponse(repository.save(entry));
    }

    public MemberAvailabilityResponse update(
            Long projectId, Long id, MemberAvailabilityRequest request) {

        Project project = getProject(projectId);
        projectAccessService.requireView(project);
        validateRequest(request);

        MemberAvailability entry = repository.findByIdAndProjectId(id, projectId)
                .orElseThrow(() -> new RuntimeException("Availability entry not found"));

        Long currentUserId = currentUserService.getCurrentUserId();
        boolean manager = isManager(project, currentUserId);

        User user;
        if (manager) {
            user = resolveUser(projectId, request.getUserId());
        } else {
            if (entry.getUser() == null || !currentUserId.equals(entry.getUser().getId())) {
                throw new RuntimeException("You can only update your own availability");
            }
            if (request.getAvailabilityType() == MemberAvailabilityType.HOLIDAY) {
                throw new RuntimeException("Only project admins can create project-wide holidays");
            }
            user = currentUserService.getCurrentUser();
            requireProjectMember(projectId, user.getId());
            requireSelfUpdateOpen(projectId, user.getId());
            requireNotPast(request.getAvailabilityDate());
        }

        validateScope(request, user);

        boolean changed = !sameUser(entry.getUser(), user)
                || !entry.getAvailabilityDate().equals(request.getAvailabilityDate());

        if (changed) {
            boolean duplicate = user != null
                    ? repository.existsByProjectIdAndUserIdAndAvailabilityDateAndIdNot(
                            projectId, user.getId(), request.getAvailabilityDate(), id)
                    : repository.existsByProjectIdAndUserIdAndAvailabilityDateAndIdNot(
                            projectId, null, request.getAvailabilityDate(), id);
            if (duplicate) {
                throw new IllegalArgumentException("Availability already exists for this member and date");
            }
        }

        entry.setUser(user);
        entry.setAvailabilityDate(request.getAvailabilityDate());
        entry.setAvailabilityType(request.getAvailabilityType());
        entry.setAvailableHours(normalizeHours(projectId, request.getAvailabilityDate(), request.getAvailabilityType(),
                request.getAvailableHours()));
        entry.setReason(normalizeReason(request.getReason()));

        return toResponse(repository.save(entry));
    }

    public void delete(Long projectId, Long id) {
        Project project = getProject(projectId);
        projectAccessService.requireManager(project);

        MemberAvailability entry = repository.findByIdAndProjectId(id, projectId)
                .orElseThrow(() -> new RuntimeException("Availability entry not found"));

        repository.delete(entry);
    }

    private User resolveUser(Long projectId, Long userId) {
        if (userId == null)
            return null;
        requireProjectMember(projectId, userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void requireProjectMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            Project project = getProject(projectId);
            if (project.getOwner() == null || !project.getOwner().getId().equals(userId)) {
                throw new RuntimeException("User is not assigned to this project");
            }
        }
    }

    private void requireSelfUpdateOpen(Long projectId, Long userId) {
        ProjectUser membership = projectUserRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("User is not assigned to this project"));
        if (!Boolean.TRUE.equals(membership.getAvailabilitySelfUpdateOpen())) {
            throw new RuntimeException(
                    "Availability submission is not open. Please contact the project administrator.");
        }
    }

    private void requireNotPast(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Past availability dates cannot be submitted or updated");
        }
    }

    private void validateScope(MemberAvailabilityRequest request, User user) {
        if (user == null && request.getAvailabilityType() != MemberAvailabilityType.HOLIDAY) {
            throw new IllegalArgumentException("Only HOLIDAY entries can apply to the entire project");
        }
    }

    private void validateRequest(MemberAvailabilityRequest request) {
        if (request.getAvailabilityDate() == null || request.getAvailabilityType() == null) {
            throw new IllegalArgumentException("Date and availability type are required");
        }
    }

    private BigDecimal normalizeHours(
            Long projectId,
            LocalDate date,
            MemberAvailabilityType type,
            BigDecimal requested) {

        return switch (type) {
            case HOLIDAY, UNAVAILABLE -> BigDecimal.ZERO;
            case HALF_DAY -> projectWorkingHoursService
                    .getEffectiveHours(projectId, date)
                    .divide(BigDecimal.valueOf(2));
            case AVAILABLE -> {
                BigDecimal workingHours = projectWorkingHoursService
                        .getEffectiveHours(projectId, date);
                if (requested == null) {
                    yield workingHours;
                }
                yield requested.max(BigDecimal.ZERO).min(workingHours);
            }
        };
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    private boolean sameUser(User a, User b) {
        if (a == null || b == null)
            return a == b;
        return a.getId().equals(b.getId());
    }

    private boolean isManager(Project project, Long userId) {
        if (userId == null)
            return false;

        if (isSystemAdmin())
            return true;

        if (project.getOwner() != null && userId.equals(project.getOwner().getId()))
            return true;
        return projectUserRepository.findByProjectIdAndUserId(project.getId(), userId)
                .map(member -> "ADMIN".equalsIgnoreCase(member.getRole())
                        || "PROJECT_ADMIN".equalsIgnoreCase(member.getRole()))
                .orElse(false);
    }

    private boolean isSystemAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Invalid availability date range");
        }
        if (startDate.plusMonths(12).isBefore(endDate)) {
            throw new IllegalArgumentException("Availability range cannot exceed 12 months");
        }
    }

    private MemberAvailabilityResponse toResponse(MemberAvailability entry) {
        return MemberAvailabilityResponse.builder()
                .id(entry.getId())
                .projectId(entry.getProject().getId())
                .projectName(entry.getProject().getName())
                .userId(entry.getUser() == null ? null : entry.getUser().getId())
                .userName(entry.getUser() == null ? null : entry.getUser().getName())
                .userEmail(entry.getUser() == null ? null : entry.getUser().getEmail())
                .availabilityDate(entry.getAvailabilityDate())
                .availabilityType(entry.getAvailabilityType())
                .availableHours(entry.getAvailableHours())
                .reason(entry.getReason())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
