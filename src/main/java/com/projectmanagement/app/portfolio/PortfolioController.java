package com.projectmanagement.app.portfolio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService s;

    @GetMapping
    public List<PortfolioResponse> list() {
        return s.list();
    }

    @PostMapping
    public PortfolioResponse create(@RequestBody PortfolioRequest r) {
        return s.create(r);
    }

    @PostMapping("/{id}/projects")
    public PortfolioResponse add(@PathVariable Long id, @RequestBody PortfolioProjectRequest r) {
        return s.add(id, r);
    }
}
