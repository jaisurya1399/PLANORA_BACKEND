package com.projectmanagement.app.ai;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai-pm")
@RequiredArgsConstructor
public class AiPmController {
    private final AiPmService service;

    @GetMapping("/project/{projectId}/analysis")
    public AiPmResponse analyze(@PathVariable Long projectId) {
        return service.analyze(projectId);
    }
}
