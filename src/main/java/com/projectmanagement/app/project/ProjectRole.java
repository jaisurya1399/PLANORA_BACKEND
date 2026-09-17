package com.projectmanagement.app.project;

/**
 * Project-scoped roles. The user may have a different role in each project.
 * MEMBER is retained only as a legacy compatibility value and is normalized
 * to TEAM_LEAD or DEVELOPER at the service boundary.
 */
public enum ProjectRole {
    PROJECT_ADMIN,
    TEAM_LEAD,
    DEVELOPER,
    VIEWER,
    @Deprecated
    MEMBER
}
