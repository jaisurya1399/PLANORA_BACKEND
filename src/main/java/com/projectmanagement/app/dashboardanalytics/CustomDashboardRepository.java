package com.projectmanagement.app.dashboardanalytics;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomDashboardRepository extends JpaRepository<CustomDashboard, Long> {
    List<CustomDashboard> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
