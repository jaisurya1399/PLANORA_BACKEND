package com.projectmanagement.app.portfolio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByOwnerIdOrderByNameAsc(Long ownerId);
}
