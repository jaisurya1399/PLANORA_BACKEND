package com.projectmanagement.app.dependency;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DependencyRepository extends JpaRepository<Dependency, Long> {
    List<Dependency> findBySourceTicketProjectIdOrTargetTicketProjectId(Long a, Long b);

    @org.springframework.data.jpa.repository.Query("select count(d) from Dependency d where d.sourceTicket.project.id=:pid or d.targetTicket.project.id=:pid")
    long countByProjectId(@org.springframework.data.repository.query.Param("pid") Long pid);
}
