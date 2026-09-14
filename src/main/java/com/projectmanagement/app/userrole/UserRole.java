package com.projectmanagement.app.userrole;

import com.projectmanagement.app.role.Role;
import com.projectmanagement.app.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "model_has_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

        public static final String USER_MODEL_TYPE = "App\\Models\\User";

        @EmbeddedId
        private UserRoleId id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @MapsId("roleId")
        @JoinColumn(name = "role_id", nullable = false)
        private Role role;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @MapsId("modelId")
        @JoinColumn(name = "model_id", nullable = false)
        private User user;

        @Column(name = "model_type", nullable = false, insertable = false, updatable = false)
        private String modelType;

        @PrePersist
        protected void onCreate() {
                if (modelType == null) {
                        modelType = USER_MODEL_TYPE;
                }
        }
}