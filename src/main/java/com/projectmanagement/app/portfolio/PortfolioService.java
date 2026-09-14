package com.projectmanagement.app.portfolio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {
    private final PortfolioRepository portfolios;
    private final PortfolioProjectRepository links;
    private final ProjectRepository projects;
    private final CurrentUserService current;

    @Transactional(readOnly = true)
    public List<PortfolioResponse> list() {
        return portfolios.findAll().stream().map(this::r).toList();
    }

    public PortfolioResponse create(PortfolioRequest x) {
        Portfolio p = Portfolio.builder().name(x.getName()).description(x.getDescription())
                .owner(current.getCurrentUser()).build();
        return r(portfolios.save(p));
    }

    public PortfolioResponse add(Long id, PortfolioProjectRequest x) {
        Portfolio p = portfolios.findById(id).orElseThrow(() -> new RuntimeException("Portfolio not found"));
        if (!p.getOwner().getId().equals(current.getCurrentUserId()))
            throw new RuntimeException("Only portfolio owner can change it");
        Project pr = projects.findById(x.getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
        if (!links.existsByPortfolioIdAndProjectId(id, x.getProjectId()))
            links.save(PortfolioProject.builder().portfolio(p).project(pr).targetPercent(x.getTargetPercent()).build());
        return r(p);
    }

    private PortfolioResponse r(Portfolio p) {
        return PortfolioResponse.builder().id(p.getId()).name(p.getName()).description(p.getDescription())
                .projects(links.findByPortfolioId(p.getId())).build();
    }
}
