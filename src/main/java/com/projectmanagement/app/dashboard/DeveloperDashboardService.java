package com.projectmanagement.app.dashboard;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectUser;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketActivity;
import com.projectmanagement.app.ticket.TicketActivityRepository;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.timesheet.TimeSheet;
import com.projectmanagement.app.timesheet.TimeSheetCell;
import com.projectmanagement.app.timesheet.TimeSheetCellRepository;
import com.projectmanagement.app.timesheet.TimeSheetRepository;
import com.projectmanagement.app.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperDashboardService {

        private final CurrentUserService currentUserService;

        private final ProjectUserRepository projectUserRepository;

        private final TicketRepository ticketRepository;

        private final TimeSheetRepository timeSheetRepository;

        private final TimeSheetCellRepository timeSheetCellRepository;

        private final TicketActivityRepository ticketActivityRepository;

        @Transactional(readOnly = true)
        public DeveloperDashboardResponse getDashboard() {

                // ============================================================
                // LOGGED-IN USER
                // ============================================================

                Long loggedInUserId = currentUserService.getCurrentUserId();

                if (loggedInUserId == null) {
                        throw new IllegalStateException(
                                        "Logged-in user not found");
                }

                // ============================================================
                // MY PROJECTS
                //
                // IMPORTANT:
                // Only projects assigned to the currently logged-in user
                // are fetched.
                // ============================================================

                List<ProjectUser> projectUsers = projectUserRepository.findByUserId(
                                loggedInUserId);

                List<DeveloperDashboardResponse.ProjectSummary> projects = projectUsers.stream()
                                .filter(projectUser -> projectUser != null)
                                .filter(projectUser -> projectUser.getProject() != null)
                                .filter(projectUser -> projectUser.getProject()
                                                .getDeletedAt() == null)
                                .map(this::buildProjectSummary)
                                .toList();

                long myProjects = projects.size();

                // ============================================================
                // MY TASKS
                //
                // Only tickets assigned to the logged-in developer
                // ============================================================

                List<Ticket> myTickets = ticketRepository
                                .findByResponsibleIdAndDeletedAtIsNull(
                                                loggedInUserId);

                long myTasks = myTickets.size();

                // ============================================================
                // PENDING TASKS
                // ============================================================

                long pendingTasks = myTickets.stream()
                                .filter(ticket -> ticket != null)
                                .filter(ticket -> ticket.getStatus() != null)
                                .filter(ticket -> {

                                        String statusName = ticket.getStatus().getName();

                                        if (statusName == null) {
                                                return true;
                                        }

                                        String status = statusName.trim().toLowerCase();

                                        return !status.equals("done")
                                                        && !status.equals("completed")
                                                        && !status.equals("closed")
                                                        && !status.equals("resolved");
                                })
                                .count();

                // ============================================================
                // HOURS THIS WEEK
                //
                // Only timesheets belonging to logged-in developer
                // ============================================================

                LocalDate today = LocalDate.now();

                LocalDate weekStart = today.with(DayOfWeek.MONDAY);

                LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

                BigDecimal hoursThisWeek = BigDecimal.ZERO;

                List<TimeSheet> timeSheets = timeSheetRepository
                                .findByUserIdAndDeletedAtIsNull(
                                                loggedInUserId);

                for (TimeSheet timeSheet : timeSheets) {

                        if (timeSheet == null) {
                                continue;
                        }

                        List<TimeSheetCell> cells = timeSheetCellRepository
                                        .findByTimeSheetId(
                                                        timeSheet.getId());

                        for (TimeSheetCell cell : cells) {

                                if (cell == null) {
                                        continue;
                                }

                                if (cell.getDate() == null) {
                                        continue;
                                }

                                if (cell.getDate().isBefore(weekStart)
                                                || cell.getDate().isAfter(weekEnd)) {
                                        continue;
                                }

                                if (cell.getValue() != null) {

                                        hoursThisWeek = hoursThisWeek.add(
                                                        cell.getValue());
                                }
                        }
                }

                // ============================================================
                // RECENT ACTIVITY
                //
                // Only activities created by logged-in developer
                // ============================================================

                List<TicketActivity> activities = ticketActivityRepository
                                .findTop10ByUserIdOrderByCreatedAtDesc(
                                                loggedInUserId);

                List<DeveloperDashboardResponse.RecentActivity> recentActivity = activities.stream()
                                .filter(activity -> activity != null)
                                .map(activity -> DeveloperDashboardResponse.RecentActivity
                                                .builder()

                                                .id(activity.getId())

                                                .ticketId(
                                                                activity.getTicket() != null
                                                                                ? activity.getTicket().getId()
                                                                                : null)

                                                .ticketName(
                                                                activity.getTicket() != null
                                                                                ? activity.getTicket().getName()
                                                                                : null)

                                                .oldStatus(
                                                                activity.getOldStatus() != null
                                                                                ? activity.getOldStatus().getName()
                                                                                : null)

                                                .newStatus(
                                                                activity.getNewStatus() != null
                                                                                ? activity.getNewStatus().getName()
                                                                                : null)

                                                .userName(
                                                                activity.getUser() != null
                                                                                ? activity.getUser().getName()
                                                                                : null)

                                                .createdAt(
                                                                activity.getCreatedAt())

                                                .build())
                                .toList();

                // ============================================================
                // FINAL RESPONSE
                // ============================================================

                return DeveloperDashboardResponse.builder()

                                .myProjects(myProjects)

                                .myTasks(myTasks)

                                .pendingTasks(pendingTasks)

                                .hoursThisWeek(hoursThisWeek)

                                .projects(projects)

                                .recentActivity(recentActivity)

                                .build();
        }

        // ============================================================
        // BUILD PROJECT RESPONSE
        // ============================================================

        private DeveloperDashboardResponse.ProjectSummary buildProjectSummary(ProjectUser projectUser) {

                Project project = projectUser.getProject();

                User owner = project.getOwner();

                String projectStatus = null;

                if (project.getStatus() != null) {
                        projectStatus = project.getStatus().getName();
                }

                return DeveloperDashboardResponse.ProjectSummary
                                .builder()

                                .id(project.getId())

                                .name(project.getName())

                                .description(project.getDescription())

                                .ticketPrefix(project.getTicketPrefix())

                                .statusType(project.getStatusType())

                                .projectStatus(projectStatus)

                                .ownerId(
                                                owner != null
                                                                ? owner.getId()
                                                                : null)

                                .ownerName(
                                                owner != null
                                                                ? owner.getName()
                                                                : null)

                                .ownerEmail(
                                                owner != null
                                                                ? owner.getEmail()
                                                                : null)

                                .assignedRole(
                                                projectUser.getRole())

                                .createdAt(
                                                project.getCreatedAt())

                                .updatedAt(
                                                project.getUpdatedAt())

                                .build();
        }
}