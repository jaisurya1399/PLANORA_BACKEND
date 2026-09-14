package com.projectmanagement.app.dependency;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dependencies")
@RequiredArgsConstructor
public class DependencyController {
    private final DependencyService s;

    @GetMapping("/project/{projectId}")
    public List<DependencyResponse> list(@PathVariable Long projectId) {
        return s.list(projectId);
    }

    @PostMapping
    public DependencyResponse save(@RequestBody DependencyRequest r) {
        return s.save(r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        s.delete(id);
    }
}
