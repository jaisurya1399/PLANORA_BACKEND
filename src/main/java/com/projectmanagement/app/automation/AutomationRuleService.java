package com.projectmanagement.app.automation;

import java.time.LocalDateTime;
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
public class AutomationRuleService {
    private final AutomationRuleRepository repo;
    private final ProjectRepository projects;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<AutomationRuleResponse> list(Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return repo.findByProjectId(projectId).stream().map(this::r).toList();
    }

    public AutomationRuleResponse save(AutomationRuleRequest x) {
        Project p = projects.findById(x.getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireManager(p);
        AutomationRule a = AutomationRule.builder().project(p).name(x.getName()).triggerEvent(x.getTriggerEvent())
                .conditionExpression(x.getConditionExpression() == null ? "ALWAYS" : x.getConditionExpression())
                .actionType(x.getActionType()).actionValue(x.getActionValue())
                .enabled(x.getEnabled() == null || x.getEnabled()).build();
        return r(repo.save(a));
    }

    public AutomationRuleResponse toggle(Long id) {
        AutomationRule a = repo.findById(id).orElseThrow(() -> new RuntimeException("Automation rule not found"));
        access.requireManager(a.getProject());
        a.setEnabled(!a.isEnabled());
        return r(a);
    }

    public AutomationRuleResponse run(Long id) {
        AutomationRule a = repo.findById(id).orElseThrow(() -> new RuntimeException("Automation rule not found"));
        access.requireManager(a.getProject());
        a.setLastRunAt(LocalDateTime.now());
        return r(a);
    }

    private AutomationRuleResponse r(AutomationRule a) {
        return AutomationRuleResponse.builder().id(a.getId()).projectId(a.getProject().getId())
                .projectName(a.getProject().getName()).name(a.getName()).triggerEvent(a.getTriggerEvent())
                .conditionExpression(a.getConditionExpression()).actionType(a.getActionType())
                .actionValue(a.getActionValue()).enabled(a.isEnabled()).lastRunAt(a.getLastRunAt()).build();
    }
}
