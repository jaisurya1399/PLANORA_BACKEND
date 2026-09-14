package com.projectmanagement.app.release;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.ticket.TicketStatusCategory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReleaseVersionService {
    private final ReleaseVersionRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<ReleaseVersionResponse> list(Long projectId) {
        getProject(projectId);
        return releaseRepository.findByProjectIdOrderByReleaseDateAscVersionAsc(projectId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReleaseVersionResponse get(Long projectId, Long id) {
        return toResponse(getRelease(projectId, id));
    }

    public ReleaseVersionResponse create(Long projectId, ReleaseVersionRequest r) {
        Project p = getProject(projectId);
        validateDates(r.getStartDate(), r.getReleaseDate());
        String version = norm(r.getVersion());
        if (releaseRepository.existsByProjectIdAndVersionIgnoreCase(projectId, version))
            throw conflict("Version already exists in this project");
        ReleaseVersion x = ReleaseVersion.builder().project(p).version(version).name(norm(r.getName()))
                .description(r.getDescription()).releaseNotes(r.getReleaseNotes()).startDate(r.getStartDate())
                .releaseDate(r.getReleaseDate()).status(r.getStatus() == null ? ReleaseStatus.PLANNED : r.getStatus())
                .build();
        return toResponse(releaseRepository.save(x));
    }

    public ReleaseVersionResponse update(Long projectId, Long id, ReleaseVersionRequest r) {
        ReleaseVersion x = getRelease(projectId, id);
        validateDates(r.getStartDate(), r.getReleaseDate());
        String version = norm(r.getVersion());
        if (releaseRepository.existsByProjectIdAndVersionIgnoreCaseAndIdNot(projectId, version, id))
            throw conflict("Another release uses this version");
        x.setVersion(version);
        x.setName(norm(r.getName()));
        x.setDescription(r.getDescription());
        x.setReleaseNotes(r.getReleaseNotes());
        x.setStartDate(r.getStartDate());
        x.setReleaseDate(r.getReleaseDate());
        if (r.getStatus() != null)
            x.setStatus(r.getStatus());
        return toResponse(releaseRepository.save(x));
    }

    public void delete(Long projectId, Long id) {
        releaseRepository.delete(getRelease(projectId, id));
    }

    public ReleaseVersionResponse updateStatus(Long projectId, Long id, ReleaseStatus status) {
        if (status == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        ReleaseVersion x = getRelease(projectId, id);
        x.setStatus(status);
        return toResponse(x);
    }

    @Transactional(readOnly = true)
    public ReleaseVersionProgressResponse progress(Long projectId, Long id) {
        ReleaseVersion x = getRelease(projectId, id);
        List<Ticket> ts = releaseRepository.findTickets(id);
        BigDecimal total = sum(ts, false), completed = sum(ts, true);
        long done = ts.stream().filter(t -> category(t) == TicketStatusCategory.DONE).count();
        long inProgress = ts.stream().filter(t -> category(t) == TicketStatusCategory.IN_PROGRESS).count();
        return ReleaseVersionProgressResponse.builder().releaseId(id).version(x.getVersion()).totalTickets(ts.size())
                .completedTickets(done).inProgressTickets(inProgress).remainingTickets(Math.max(0, ts.size() - done))
                .totalEstimation(scale(total)).completedEstimation(scale(completed))
                .remainingEstimation(scale(total.subtract(completed).max(BigDecimal.ZERO)))
                .progressPercent(percent(done, ts.size())).build();
    }

    @Transactional(readOnly = true)
    public ReleaseBurndownResponse burndown(Long projectId, Long id) {
        ReleaseVersion x = getRelease(projectId, id);
        List<Ticket> ts = releaseRepository.findTickets(id);
        LocalDate start = x.getStartDate();
        if (start == null)
            start = ts.stream().map(Ticket::getCreatedAt).filter(Objects::nonNull).map(LocalDateTime::toLocalDate)
                    .min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate end = x.getReleaseDate();
        if (end == null)
            end = LocalDate.now();
        if (end.isBefore(start))
            end = start;
        LocalDate chartEnd = LocalDate.now().isBefore(end) ? LocalDate.now() : end;
        if (chartEnd.isBefore(start))
            chartEnd = start;
        BigDecimal total = sum(ts, false);
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end));
        List<ReleaseBurndownResponse.Point> points = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(chartEnd); d = d.plusDays(1)) {
            BigDecimal completed = completedByDate(ts, d);
            BigDecimal remaining = total.subtract(completed).max(BigDecimal.ZERO);
            long elapsed = Math.max(0, ChronoUnit.DAYS.between(start, d));
            BigDecimal ideal = total.subtract(total.multiply(BigDecimal.valueOf(elapsed))
                    .divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO);
            points.add(ReleaseBurndownResponse.Point.builder().date(d).idealRemaining(scale(ideal))
                    .remaining(scale(remaining)).completed(scale(completed)).build());
        }
        return ReleaseBurndownResponse.builder().releaseId(id).version(x.getVersion()).startsAt(start).endsAt(end)
                .totalEstimation(scale(total)).points(points).build();
    }

    public List<ReleaseTicketResponse> tickets(Long projectId, Long id) {
        getRelease(projectId, id);
        return releaseRepository
                .findTickets(id).stream().map(
                        t -> ReleaseTicketResponse.builder().id(t.getId()).code(t.getCode()).name(t.getName())
                                .status(t.getStatus() == null ? null : t.getStatus().getName())
                                .statusCategory(t.getStatus() == null || t.getStatus().getCategory() == null ? null
                                        : t.getStatus().getCategory().name())
                                .estimation(t.getEstimation()).build())
                .toList();
    }

    public void assignTicket(Long projectId, Long id, Long ticketId) {
        ReleaseVersion x = getRelease(projectId, id);
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        if (!Objects.equals(t.getProject().getId(), projectId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket belongs to another project");
        t.setRelease(x);
        ticketRepository.save(t);
    }

    public void removeTicket(Long projectId, Long id, Long ticketId) {
        getRelease(projectId, id);
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        if (t.getRelease() != null && Objects.equals(t.getRelease().getId(), id)) {
            t.setRelease(null);
            ticketRepository.save(t);
        }
    }

    private ReleaseVersion getRelease(Long p, Long id) {
        getProject(p);
        return releaseRepository.findByIdAndProjectId(id, p)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Release not found"));
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private void validateDates(LocalDate s, LocalDate e) {
        if (s != null && e != null && e.isBefore(s))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date cannot be before start date");
    }

    private String norm(String s) {
        return s == null ? "" : s.trim();
    }

    private ResponseStatusException conflict(String m) {
        return new ResponseStatusException(HttpStatus.CONFLICT, m);
    }

    private ReleaseVersionResponse toResponse(ReleaseVersion x) {
        long total = releaseRepository.countTickets(x.getId()),
                done = releaseRepository.countCompletedTickets(x.getId());
        return ReleaseVersionResponse.builder().id(x.getId()).version(x.getVersion()).name(x.getName())
                .description(x.getDescription()).releaseNotes(x.getReleaseNotes()).projectId(x.getProject().getId())
                .projectName(x.getProject().getName()).startDate(x.getStartDate()).releaseDate(x.getReleaseDate())
                .status(x.getStatus()).totalTickets(total).completedTickets(done)
                .remainingTickets(Math.max(0, total - done)).progressPercent(percent(done, total))
                .createdAt(x.getCreatedAt()).updatedAt(x.getUpdatedAt()).build();
    }

    private TicketStatusCategory category(Ticket t) {
        return t.getStatus() == null ? null : t.getStatus().getCategory();
    }

    private BigDecimal sum(List<Ticket> ts, boolean done) {
        return ts.stream().filter(t -> !done || category(t) == TicketStatusCategory.DONE)
                .map(t -> t.getEstimation() == null ? BigDecimal.ZERO : t.getEstimation())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal completedByDate(List<Ticket> ts, LocalDate d) {
        return ts.stream().filter(t -> category(t) == TicketStatusCategory.DONE).filter(t -> {
            LocalDateTime dt = t.getResolvedAt() != null ? t.getResolvedAt() : t.getUpdatedAt();
            return dt != null && !dt.toLocalDate().isAfter(d);
        }).map(t -> t.getEstimation() == null ? BigDecimal.ZERO : t.getEstimation()).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    private int percent(long a, long b) {
        return b == 0 ? 0 : (int) Math.round(a * 100.0 / b);
    }

    private BigDecimal scale(BigDecimal x) {
        return x.setScale(2, RoundingMode.HALF_UP);
    }
}
