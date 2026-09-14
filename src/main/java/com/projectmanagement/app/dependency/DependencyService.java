package com.projectmanagement.app.dependency;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DependencyService {
    private final DependencyRepository repo;
    private final TicketRepository tickets;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<DependencyResponse> list(Long pid) {
        List<Dependency> ds = repo.findBySourceTicketProjectIdOrTargetTicketProjectId(pid, pid);
        return ds.stream().map(this::r).toList();
    }

    public DependencyResponse save(DependencyRequest x) {
        Ticket s = tickets.findById(x.getSourceTicketId())
                .orElseThrow(() -> new RuntimeException("Source ticket not found"));
        Ticket t = tickets.findById(x.getTargetTicketId())
                .orElseThrow(() -> new RuntimeException("Target ticket not found"));
        if (s.getId().equals(t.getId()))
            throw new RuntimeException("A ticket cannot depend on itself");
        access.requireEditor(s.getProject());
        access.requireEditor(t.getProject());
        if (wouldCreateCycle(s.getId(), t.getId()))
            throw new RuntimeException("Dependency would create a cycle");
        return r(repo.save(Dependency.builder().sourceTicket(s).targetTicket(t)
                .type(x.getType() == null ? "BLOCKS" : x.getType()).description(x.getDescription()).build()));
    }

    private boolean wouldCreateCycle(Long source, Long target) {
        Set<Long> seen = new HashSet<>();
        Deque<Long> q = new ArrayDeque<>();
        q.add(target);
        while (!q.isEmpty()) {
            Long cur = q.removeFirst();
            if (!seen.add(cur))
                continue;
            if (cur.equals(source))
                return true;
            repo.findAll().stream().filter(d -> d.getSourceTicket().getId().equals(cur))
                    .forEach(d -> q.add(d.getTargetTicket().getId()));
        }
        return false;
    }

    public void delete(Long id) {
        Dependency d = repo.findById(id).orElseThrow(() -> new RuntimeException("Dependency not found"));
        access.requireEditor(d.getSourceTicket().getProject());
        repo.delete(d);
    }

    private DependencyResponse r(Dependency d) {
        return DependencyResponse.builder().id(d.getId()).sourceTicketId(d.getSourceTicket().getId())
                .sourceCode(d.getSourceTicket().getCode()).targetTicketId(d.getTargetTicket().getId())
                .targetCode(d.getTargetTicket().getCode()).type(d.getType()).description(d.getDescription()).build();
    }
}
