package com.projectmanagement.app.customfield;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreenFieldRepository extends JpaRepository<ScreenField, Long> {
    List<ScreenField> findByScreenIdOrderByDisplayOrderAscIdAsc(Long screenId);

    boolean existsByScreenIdAndFieldId(Long screenId, Long fieldId);
}
