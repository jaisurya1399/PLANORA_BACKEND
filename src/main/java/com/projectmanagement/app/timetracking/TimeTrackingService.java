package com.projectmanagement.app.timetracking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketHour;
import com.projectmanagement.app.ticket.TicketHourRepository;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeTrackingService {
    private final TicketRepository ticketRepository;
    private final TicketHourRepository ticketHourRepository;
    private final TimeTrackingTimerRepository timerRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;

    @Transactional(readOnly = true)
    public TimeTrackingSummaryResponse summary(Long ticketId) {
        Ticket ticket = getTicket(ticketId);
        projectAccessService.requireView(ticket.getProject());
        BigDecimal actual = actual(ticketId);
        BigDecimal estimate = nz(ticket.getEstimation());
        BigDecimal remaining = estimate.subtract(actual).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return TimeTrackingSummaryResponse.builder()
                .ticketId(ticket.getId()).ticketCode(ticket.getCode()).ticketName(ticket.getName())
                .estimatedHours(estimate).actualHours(actual).remainingEstimateHours(remaining)
                .varianceHours(actual.subtract(estimate).setScale(2, RoundingMode.HALF_UP)).build();
    }

    public TimeTrackingTimerResponse start(Long ticketId, String description) {
        Ticket ticket = getTicket(ticketId);
        projectAccessService.requireEditor(ticket.getProject());
        User user = currentUserService.getCurrentUser();
        timerRepository.findByUserId(user.getId()).ifPresent(t -> {
            throw new IllegalStateException("You already have an active timer");
        });
        TimeTrackingTimer timer = TimeTrackingTimer.builder().ticket(ticket).user(user)
                .startedAt(LocalDateTime.now()).description(description).build();
        return toTimer(timerRepository.save(timer));
    }

    public TicketHour stop() {
        User user = currentUserService.getCurrentUser();
        TimeTrackingTimer timer = timerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("No active timer found"));
        long seconds = Math.max(60, Duration.between(timer.getStartedAt(), LocalDateTime.now()).getSeconds());
        BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        TicketHour hour = TicketHour.builder().ticket(timer.getTicket()).user(user).value(hours)
                .comment(timer.getDescription()).build();
        timerRepository.delete(timer);
        return ticketHourRepository.save(hour);
    }

    @Transactional(readOnly = true)
    public TimeTrackingTimerResponse activeTimer() {
        return timerRepository.findByUserId(currentUserService.getCurrentUserId()).map(this::toTimer).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TimeTrackingReportRow> report(Long projectId, Long userId, LocalDate from, LocalDate to) {
        LocalDateTime start = from != null ? from.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : LocalDateTime.now();
        List<TicketHour> hours = ticketHourRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        if (projectId != null)
            projectAccessService.requireView(getTicketProject(projectId));
        return hours.stream()
                .filter(h -> projectId == null || h.getTicket().getProject().getId().equals(projectId))
                .filter(h -> userId == null || h.getUser().getId().equals(userId))
                .collect(Collectors.groupingBy(h -> h.getTicket().getId() + ":" + h.getUser().getId()))
                .values().stream().map(list -> {
                    Ticket ticket = list.get(0).getTicket();
                    BigDecimal actual = list.stream().map(TicketHour::getValue).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal estimate = nz(ticket.getEstimation());
                    return TimeTrackingReportRow.builder().ticketId(ticket.getId()).ticketCode(ticket.getCode())
                            .ticketName(ticket.getName()).userId(list.get(0).getUser().getId())
                            .userName(list.get(0).getUser().getName()).userEmail(list.get(0).getUser().getEmail())
                            .estimatedHours(estimate).actualHours(actual)
                            .remainingEstimateHours(
                                    estimate.subtract(actual).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                            .varianceHours(actual.subtract(estimate).setScale(2, RoundingMode.HALF_UP)).build();
                })
                .sorted(Comparator.comparing(TimeTrackingReportRow::getTicketCode,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private BigDecimal actual(Long ticketId) {
        return ticketHourRepository.findByTicketId(ticketId).stream().map(TicketHour::getValue)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private com.projectmanagement.app.project.Project getTicketProject(Long projectId) {
        return ticketRepository.findByProjectIdAndDeletedAtIsNullOrderByOrderAsc(projectId).stream().findFirst()
                .map(Ticket::getProject)
                .orElseThrow(() -> new RuntimeException("Project not found or has no active tickets"));
    }

    private Ticket getTicket(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private TimeTrackingTimerResponse toTimer(TimeTrackingTimer t) {
        long elapsed = Math.max(0, Duration.between(t.getStartedAt(), LocalDateTime.now()).getSeconds());
        return TimeTrackingTimerResponse.builder().id(t.getId()).ticketId(t.getTicket().getId())
                .ticketCode(t.getTicket().getCode())
                .ticketName(t.getTicket().getName()).userId(t.getUser().getId()).startedAt(t.getStartedAt())
                .description(t.getDescription()).elapsedSeconds(elapsed).build();
    }
}
