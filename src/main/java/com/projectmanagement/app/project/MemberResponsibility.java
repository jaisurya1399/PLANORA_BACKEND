package com.projectmanagement.app.project;

/**
 * Responsibilities available to users whose project access level is MEMBER.
 * These are not system roles and do not grant access outside the project.
 */
public enum MemberResponsibility {
    DEVELOPER,
    TESTER,
    TEAM_LEAD,
    SCRUM_MASTER,
    PRODUCT_OWNER,
    BUSINESS_ANALYST
}
