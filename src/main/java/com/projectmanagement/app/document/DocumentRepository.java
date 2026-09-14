package com.projectmanagement.app.document;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository
                extends JpaRepository<Document, Long> {

        List<Document> findByNameContainingIgnoreCase(String name);

        List<Document> findByOriginalNameContainingIgnoreCase(
                        String originalName);

        List<Document> findByContentType(String contentType);

        long countByContentType(String contentType);
}