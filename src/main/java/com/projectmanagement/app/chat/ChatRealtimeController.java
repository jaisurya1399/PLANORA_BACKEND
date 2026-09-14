package com.projectmanagement.app.chat;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.realtime.RealtimeEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat/realtime")
@RequiredArgsConstructor
public class ChatRealtimeController {
    private final RealtimeEventService events;
    private final CurrentUserService current;
    private final ProjectRepository projects;
    private final ProjectAccessService access;

    @GetMapping(value = "/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter user() {
        return events.subscribeUser(current.getCurrentUserId());
    }

    @GetMapping(value = "/project/{projectId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter project(@PathVariable Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return events.subscribeProject(projectId);
    }
}
