package com.projectmanagement.app.setting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository
        extends JpaRepository<Setting, Long> {

    List<Setting> findByGroup(String group);

    List<Setting> findByGroupOrderByNameAsc(String group);

    Optional<Setting> findByGroupAndName(
            String group,
            String name);

    Optional<Setting> findByName(String name);

    boolean existsByGroupAndName(
            String group,
            String name);

    boolean existsByName(String name);

    List<Setting> findByLocked(Boolean locked);

    List<Setting> findByGroupAndLocked(
            String group,
            Boolean locked);

    long countByGroup(String group);

    long countByLocked(Boolean locked);
}