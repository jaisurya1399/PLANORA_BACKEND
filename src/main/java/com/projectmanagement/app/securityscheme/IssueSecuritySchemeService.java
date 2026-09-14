package com.projectmanagement.app.securityscheme;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueSecuritySchemeService {
    private final IssueSecuritySchemeRepository repo;
    private final ProjectRepository projects;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public IssueSecuritySchemeResponse get(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        return repo.findByProjectId(pid).map(this::out).orElseThrow(() -> nf("Issue security scheme not found"));
    }

    public IssueSecuritySchemeResponse save(IssueSecuritySchemeRequest r) {
        Project p = projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        access.requireManager(p);
        IssueSecurityScheme s = repo.findByProjectId(p.getId())
                .orElse(IssueSecurityScheme.builder().project(p).build());
        s.setName(r.name.trim());
        s.setDefaultLevel(r.defaultLevel);
        s.setActive(r.active == null || r.active);
        return out(repo.save(s));
    }

    private IssueSecuritySchemeResponse out(IssueSecurityScheme s) {
        return IssueSecuritySchemeResponse.builder().id(s.id).projectId(s.project.getId())
                .projectName(s.project.getName()).name(s.name).defaultLevel(s.defaultLevel).active(s.active).build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
