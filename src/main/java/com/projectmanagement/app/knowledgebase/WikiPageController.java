package com.projectmanagement.app.knowledgebase;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wiki")
@RequiredArgsConstructor
public class WikiPageController {
    private final WikiPageService s;

    @GetMapping("/project/{projectId}")
    public List<WikiPageResponse> list(@PathVariable Long projectId) {
        return s.list(projectId);
    }

    @PostMapping
    public WikiPageResponse create(@RequestBody WikiPageRequest r) {
        return s.save(null, r);
    }

    @PutMapping("/{id}")
    public WikiPageResponse update(@PathVariable Long id, @RequestBody WikiPageRequest r) {
        return s.save(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        s.delete(id);
    }
}
