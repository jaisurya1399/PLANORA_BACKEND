package com.projectmanagement.app.ticket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-comments")
@RequiredArgsConstructor
public class TicketMentionController {
    private final TicketRepository tickets;
    private final ProjectUserRepository projectUsers;
    private final UserRepository users;
    private final ProjectAccessService access;

    @GetMapping("/ticket/{ticketId}/mentions")
    @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
    public List<TicketMentionResponse> suggestions(@PathVariable Long ticketId,
            @RequestParam(defaultValue = "") String q) {
        Ticket t = tickets.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        access.requireView(t.getProject());
        String needle = q == null ? "" : q.trim().toLowerCase();
        Set<Long> ids = new HashSet<>();
        List<User> list = new ArrayList<>();
        if (t.getProject().getOwner() != null) {
            ids.add(t.getProject().getOwner().getId());
            list.add(t.getProject().getOwner());
        }
        projectUsers.findByProjectId(t.getProject().getId()).forEach(p -> {
            if (p.getUser() != null && ids.add(p.getUser().getId()))
                list.add(p.getUser());
        });
        return list.stream().filter(u -> u.getDeletedAt() == null)
                .filter(u -> needle.isBlank() || u.getName().toLowerCase().contains(needle)
                        || u.getEmail().toLowerCase().contains(needle))
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER)).limit(10)
                .map(u -> TicketMentionResponse.builder().id(u.getId()).name(u.getName()).email(u.getEmail())
                        .mention("@" + u.getEmail()).build())
                .toList();
    }
}
