package com.projectmanagement.app.team;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTeamMemberRepository extends JpaRepository<ProjectTeamMember, Long> {
    List<ProjectTeamMember> findByTeamId(Long teamId);

    Optional<ProjectTeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
}
