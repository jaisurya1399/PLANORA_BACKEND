package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketCommentReactionService {
    private final TicketCommentReactionRepository repo;
    private final TicketCommentRepository comments;
    private final CurrentUserService current;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<TicketCommentReactionResponse> get(Long commentId) {
        TicketComment c = comment(commentId);
        access.requireView(c.getTicket().getProject());
        return repo.findByCommentIdOrderByCreatedAtAsc(commentId).stream().map(this::map).toList();
    }

    public TicketCommentReactionResponse toggle(Long commentId, String raw) {
        TicketComment c = comment(commentId);
        access.requireEditor(c.getTicket().getProject());
        String r = normalize(raw);
        User u = current.getCurrentUser();
        var existing = repo.findByCommentIdAndUserIdAndReaction(commentId, u.getId(), r);
        if (existing.isPresent()) {
            repo.delete(existing.get());
            return null;
        }
        return map(repo.save(TicketCommentReaction.builder().comment(c).user(u).reaction(r).build()));
    }

    private TicketComment comment(Long id) {
        return comments.findById(id).orElseThrow(() -> new RuntimeException("Comment not found: " + id));
    }

    private String normalize(String s) {
        if (s == null || s.isBlank())
            throw new IllegalArgumentException("Reaction is required");
        String r = s.trim();
        if (r.length() > 32)
            throw new IllegalArgumentException("Reaction is too long");
        return r;
    }

    private TicketCommentReactionResponse map(TicketCommentReaction r) {
        return TicketCommentReactionResponse.builder().id(r.getId()).commentId(r.getComment().getId())
                .userId(r.getUser().getId()).userName(r.getUser().getName()).reaction(r.getReaction())
                .createdAt(r.getCreatedAt()).build();
    }
}
