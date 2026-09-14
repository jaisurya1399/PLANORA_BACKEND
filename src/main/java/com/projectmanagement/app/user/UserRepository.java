package com.projectmanagement.app.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findActiveUsers();

    @Query(value = """
            SELECT r.name
            FROM roles r
            INNER JOIN model_has_roles mhr
                ON mhr.role_id = r.id
            WHERE mhr.model_id = :userId
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findRoleNameByUserId(@Param("userId") Long userId);
}