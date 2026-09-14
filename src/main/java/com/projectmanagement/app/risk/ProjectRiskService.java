package com.projectmanagement.app.risk;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRiskService {
    private final ProjectRiskRepository repo;
    private final ProjectRepository projects;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<ProjectRisk> list(Long id) {
        Project p = projects.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return repo.findByProjectIdOrderByCreatedAtDesc(id);
    }

    public ProjectRisk save(ProjectRiskRequest x) {
        Project p = projects.findById(x.getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireManager(p);
        return repo.save(ProjectRisk.builder().project(p).title(x.getTitle()).description(x.getDescription())
                .probability(x.getProbability()).impact(x.getImpact())
                .status(x.getStatus() == null ? "OPEN" : x.getStatus()).owner(x.getOwner())
                .mitigation(x.getMitigation()).build());
    }
}
