package com.projectmanagement.app.customfield;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ScreenConfigurationRequest {
    @NotBlank
    private String name;
    @Positive
    private Long projectId;
    @Positive
    private Long ticketTypeId;
    private Boolean active;
}
