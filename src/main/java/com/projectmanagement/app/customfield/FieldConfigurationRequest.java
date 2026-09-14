package com.projectmanagement.app.customfield;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FieldConfigurationRequest {
    @NotNull
    @Positive
    private Long fieldId;
    @Positive
    private Long projectId;
    @Positive
    private Long ticketTypeId;
    private Boolean visible;
    private Boolean required;
    private Integer displayOrder;
}
