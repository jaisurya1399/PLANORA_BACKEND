package com.projectmanagement.app.customfield;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldRepository extends JpaRepository<CustomField, Long> {
    Optional<CustomField> findByKey(String key);

    List<CustomField> findByActiveTrueOrderByNameAsc();

    List<CustomField> findAllByOrderByNameAsc();

    boolean existsByKey(String key);
}
