package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, Long> {
    List<ProjectTemplate> findAllByOrderByUpdatedAtDesc();

    List<ProjectTemplate> findByCreatedByIdOrderByUpdatedAtDesc(Long createdById);

    boolean existsByNameIgnoreCaseAndCreatedById(String name, Long createdById);

    boolean existsByNameIgnoreCaseAndCreatedByIdAndIdNot(String name, Long createdById, Long id);
}
