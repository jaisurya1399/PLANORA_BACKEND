package com.projectmanagement.app.knowledgebase;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiPageRepository extends JpaRepository<WikiPage, Long> {
    List<WikiPage> findByProjectIdOrderByTitleAsc(Long projectId);
}
