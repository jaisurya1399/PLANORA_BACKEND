package com.projectmanagement.app.usergroup;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.projectmanagement.app.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_groups", uniqueConstraints = @UniqueConstraint(name = "uk_user_groups_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"), uniqueConstraints = @UniqueConstraint(name = "uk_user_group_member", columnNames = {
            "group_id", "user_id" }))
    @Builder.Default
    private Set<User> members = new HashSet<>();
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void create() {
        LocalDateTime n = LocalDateTime.now();
        if (createdAt == null)
            createdAt = n;
        if (updatedAt == null)
            updatedAt = n;
    }

    @PreUpdate
    void update() {
        updatedAt = LocalDateTime.now();
    }
}
