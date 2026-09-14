package com.projectmanagement.app.realtime;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.*;

@RestController @RequestMapping("/api/realtime")
public class RealtimeController {
    private final RealtimeEventService events; private final ProjectRepository projectRepository; private final ProjectAccessService access; private final CurrentUserService currentUser;
    public RealtimeController(RealtimeEventService events, ProjectRepository projectRepository, ProjectAccessService access, CurrentUserService currentUser) { this.events = events; this.projectRepository = projectRepository; this.access = access; this.currentUser = currentUser; }
    @GetMapping(value = "/projects/{projectId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE) @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public SseEmitter project(@PathVariable Long projectId) { Project p = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found")); access.requireView(p); return events.subscribeProject(projectId); }
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications() { return events.subscribeUser(currentUser.getCurrentUserId()); }
}
