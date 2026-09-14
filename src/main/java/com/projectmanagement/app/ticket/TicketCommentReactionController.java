package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-comment-reactions")
@RequiredArgsConstructor
public class TicketCommentReactionController {
    private final TicketCommentReactionService service;

    @GetMapping("/comment/{commentId}")
    @PreAuthorize("hasAuthority('ticket_comment.view') or hasRole('ADMIN')")
    public List<TicketCommentReactionResponse> get(@PathVariable Long commentId) {
        return service.get(commentId);
    }

    @PostMapping("/comment/{commentId}/toggle")
    @PreAuthorize("hasAuthority('ticket_comment.update') or hasRole('ADMIN')")
    public ResponseEntity<TicketCommentReactionResponse> toggle(@PathVariable Long commentId,
            @Valid @RequestBody TicketCommentReactionRequest request) {
        var r = service.toggle(commentId, request.getReaction());
        return r == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(r);
    }
}
