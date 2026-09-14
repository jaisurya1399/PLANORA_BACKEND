package com.projectmanagement.app.automation;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/automation")
@RequiredArgsConstructor
public class AutomationRuleController {
    private final AutomationRuleService s;
    private final AutomationEngine engine;

    @GetMapping("/project/{projectId}")
    public List<AutomationRuleResponse> list(@PathVariable Long projectId) {
        return s.list(projectId);
    }

    @PostMapping
    public ResponseEntity<AutomationRuleResponse> save(@RequestBody AutomationRuleRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(s.save(r));
    }

    @PostMapping("/{id}/toggle")
    public AutomationRuleResponse toggle(@PathVariable Long id) {
        return s.toggle(id);
    }

    @PostMapping("/{id}/run")
    public AutomationRuleResponse run(@PathVariable Long id) {
        return s.run(id);
    }

    @PostMapping("/events")
    public Map<String, Integer> event(@RequestBody AutomationEventRequest r) {
        return Map.of("executed", engine.process(r));
    }
}
