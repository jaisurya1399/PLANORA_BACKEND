package com.projectmanagement.app.portfolio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioProjectRepository extends JpaRepository<PortfolioProject, Long> {
    List<PortfolioProject> findByPortfolioId(Long id);

    boolean existsByPortfolioIdAndProjectId(Long a, Long b);
}
