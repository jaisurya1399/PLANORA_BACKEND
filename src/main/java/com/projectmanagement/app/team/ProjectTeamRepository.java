package com.projectmanagement.app.team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTeamRepository extends JpaRepository<ProjectTeam, Long> {
    List<ProjectTeam> findByProjectIdAndActiveTrueOrderByNameAsc(Long projectId);
}
