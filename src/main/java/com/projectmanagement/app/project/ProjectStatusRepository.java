package com.projectmanagement.app.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectStatusRepository
                extends JpaRepository<ProjectStatus, Long> {

        List<ProjectStatus> findByDeletedAtIsNull();

        List<ProjectStatus> findByIsDefaultTrue();

        Optional<ProjectStatus> findByName(String name);

        Optional<ProjectStatus> findByNameAndDeletedAtIsNull(
                        String name);

        boolean existsByName(String name);

        boolean existsByNameAndIdNot(
                        String name,
                        Long id);

        List<ProjectStatus> findByColor(String color);
}