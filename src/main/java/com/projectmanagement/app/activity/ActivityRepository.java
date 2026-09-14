package com.projectmanagement.app.activity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByDeletedAtIsNullOrderByIdAsc();

    List<Activity> findAllByOrderByIdAsc();

    Optional<Activity> findByIdAndDeletedAtIsNull(Long id);

    Optional<Activity> findByNameIgnoreCase(String name);

    Optional<Activity> findByNameIgnoreCaseAndDeletedAtIsNull(String name);

    List<Activity> findByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc();

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
            String name,
            Long id);
}