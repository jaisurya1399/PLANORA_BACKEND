package com.projectmanagement.app.epic;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EpicRepository extends JpaRepository<Epic, Long> {

    List<Epic> findByDeletedAtIsNull();

    List<Epic> findByProjectId(Long projectId);

    List<Epic> findByProjectIdAndDeletedAtIsNull(
            Long projectId);

    List<Epic> findByParentId(Long parentId);

    List<Epic> findByParentIdAndDeletedAtIsNull(
            Long parentId);

    List<Epic> findByProjectIdAndParentId(
            Long projectId,
            Long parentId);

    List<Epic> findByProjectIdAndParentIsNull(
            Long projectId);

    Optional<Epic> findByProjectIdAndName(
            Long projectId,
            String name);

    Optional<Epic> findByProjectIdAndNameAndDeletedAtIsNull(
            Long projectId,
            String name);

    boolean existsByProjectIdAndName(
            Long projectId,
            String name);

    boolean existsByProjectIdAndNameAndIdNot(
            Long projectId,
            String name,
            Long id);

    long countByProjectId(Long projectId);

    long countByParentId(Long parentId);

    void deleteByProjectId(Long projectId);
}