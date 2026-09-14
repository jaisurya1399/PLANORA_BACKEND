package com.projectmanagement.app.priorityscheme;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.TicketPriorityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectPrioritySchemeService {
    private final ProjectPrioritySchemeRepository repo;
    private final ProjectRepository projects;
    private final TicketPriorityRepository priorities;
    private final ProjectAccessService access;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ProjectPrioritySchemeResponse get(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        ProjectPriorityScheme s = repo.findByProjectId(pid).orElseThrow(() -> nf("Priority scheme not found"));
        return out(s);
    }

    public ProjectPrioritySchemeResponse save(ProjectPrioritySchemeRequest r) {
        Project p = projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        access.requireManager(p);
        for (Long id : r.priorityIds)
            if (!priorities.existsById(id))
                throw nf("Priority not found: " + id);
        ProjectPriorityScheme s = repo.findByProjectId(p.getId())
                .orElse(ProjectPriorityScheme.builder().project(p).build());
        s.setName(r.name.trim());
        try {
            s.setPriorityIdsJson(mapper.writeValueAsString(r.priorityIds));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority scheme");
        }
        s.setActive(r.active == null || r.active);
        return out(repo.save(s));
    }

    private List<Long> ids(String s) {
        try {
            return s == null ? List.of() : mapper.readValue(s, new TypeReference<List<Long>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private ProjectPrioritySchemeResponse out(ProjectPriorityScheme s) {
        return ProjectPrioritySchemeResponse.builder().id(s.getId()).projectId(s.getProject().getId())
                .projectName(s.getProject().getName()).name(s.getName()).priorityIds(ids(s.getPriorityIdsJson()))
                .active(s.getActive()).build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
