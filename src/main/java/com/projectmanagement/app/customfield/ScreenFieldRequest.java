package com.projectmanagement.app.customfield;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ScreenFieldRequest {
    @NotNull
    @Positive
    private Long fieldId;
    private Integer displayOrder;
    private Boolean visible;
}
