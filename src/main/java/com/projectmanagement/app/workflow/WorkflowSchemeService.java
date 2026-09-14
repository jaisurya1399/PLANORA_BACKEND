package com.projectmanagement.app.workflow;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowSchemeService {
    private final WorkflowSchemeRepository repo;
    private final WorkflowRuleRepository rules;
    private final ProjectRepository projects;
    private final ProjectAccessService access;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<WorkflowSchemeResponse> list(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        return repo.findByProjectIdAndActiveTrueOrderByNameAsc(pid).stream().map(this::out).toList();
    }

    public WorkflowSchemeResponse save(WorkflowSchemeRequest r) {
        Project p = projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        access.requireManager(p);
        r.ruleIds.forEach(id -> rules.findById(id).orElseThrow(() -> nf("Workflow rule not found: " + id)));
        WorkflowScheme s = repo.findByProjectIdAndActiveTrueOrderByNameAsc(p.getId()).stream().findFirst()
                .orElse(WorkflowScheme.builder().project(p).build());
        try {
            s.setRuleIdsJson(mapper.writeValueAsString(r.ruleIds));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid workflow scheme");
        }
        s.setName(r.name.trim());
        s.setActive(r.active == null || r.active);
        return out(repo.save(s));
    }

    private List<Long> ids(String j) {
        try {
            return j == null ? List.of() : mapper.readValue(j, new TypeReference<List<Long>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private WorkflowSchemeResponse out(WorkflowScheme s) {
        return WorkflowSchemeResponse.builder().id(s.id).projectId(s.project.getId()).projectName(s.project.getName())
                .name(s.name).ruleIds(ids(s.ruleIdsJson)).active(s.active).build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
