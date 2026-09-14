package com.projectmanagement.app.metadata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationMetadataRepository
        extends JpaRepository<ApplicationMetadata, Long> {

    Optional<ApplicationMetadata> findByKey(String key);

    boolean existsByKey(String key);

    void deleteByKey(String key);
}
