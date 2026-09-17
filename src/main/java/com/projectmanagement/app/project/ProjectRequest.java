package com.projectmanagement.app.project;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Owner ID is required")
    @Positive(message = "Owner ID must be greater than zero")
    private Long ownerId;

    /** Project Admin memberships to create with the project. 1-2 are allowed. */
    @Size(min = 1, max = 2, message = "Select between 1 and 2 Project Admins")
    private List<@Positive Long> projectAdminIds;

    @NotNull(message = "Status ID is required")
    @Positive(message = "Status ID must be greater than zero")
    private Long statusId;

    @NotBlank(message = "Ticket prefix is required")
    @Size(max = 255, message = "Ticket prefix must not exceed 255 characters")
    private String ticketPrefix;

    @Size(max = 255, message = "Status type must not exceed 255 characters")
    private String statusType;
}
