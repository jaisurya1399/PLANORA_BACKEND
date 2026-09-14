package com.projectmanagement.app.securityscheme;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectPermissionSchemeService {
    private final ProjectPermissionSchemeRepository repo;
    private final ProjectRepository projects;
    private final ProjectAccessService access;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ProjectPermissionSchemeResponse get(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        ProjectPermissionScheme s = repo.findByProjectId(pid).orElseThrow(() -> nf("Permission scheme not found"));
        return out(s);
    }

    public ProjectPermissionSchemeResponse save(ProjectPermissionSchemeRequest r) {
        Project p = projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        access.requireManager(p);
        try {
            mapper.readTree(r.grantsJson);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grantsJson must be valid JSON");
        }
        ProjectPermissionScheme s = repo.findByProjectId(p.getId())
                .orElse(ProjectPermissionScheme.builder().project(p).build());
        s.setName(r.name.trim());
        s.setGrantsJson(r.grantsJson);
        s.setActive(r.active == null || r.active);
        return out(repo.save(s));
    }

    private ProjectPermissionSchemeResponse out(ProjectPermissionScheme s) {
        return ProjectPermissionSchemeResponse.builder().id(s.id).projectId(s.project.getId())
                .projectName(s.project.getName()).name(s.name).grantsJson(s.grantsJson).active(s.active).build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
