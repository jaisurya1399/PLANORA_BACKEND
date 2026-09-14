package com.projectmanagement.app.sla;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaController {
    private final SlaService s;

    @GetMapping("/project/{projectId}/policies")
    public List<SlaPolicy> policies(@PathVariable Long projectId) {
        return s.policies(projectId);
    }

    @PostMapping("/policies")
    public SlaPolicy save(@RequestBody SlaPolicyRequest r) {
        return s.save(r);
    }

    @GetMapping("/project/{projectId}/evaluate")
    public List<SlaTicketResponse> evaluate(@PathVariable Long projectId) {
        return s.evaluate(projectId);
    }
}
