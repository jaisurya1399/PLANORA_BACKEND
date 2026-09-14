package com.projectmanagement.app.risk;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
public class ProjectRiskController {
    private final ProjectRiskService risks;
    private final ProjectHealthService health;

    @GetMapping("/project/{projectId}")
    public List<ProjectRisk> list(@PathVariable Long projectId) {
        return risks.list(projectId);
    }

    @PostMapping
    public ProjectRisk save(@RequestBody ProjectRiskRequest r) {
        return risks.save(r);
    }

    @GetMapping("/project/{projectId}/health")
    public ProjectHealthResponse health(@PathVariable Long projectId) {
        return health.health(projectId);
    }
}
