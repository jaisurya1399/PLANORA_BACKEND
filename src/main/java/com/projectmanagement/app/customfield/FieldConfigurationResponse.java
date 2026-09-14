package com.projectmanagement.app.customfield;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldConfigurationResponse {
    private Long id, fieldId, projectId, ticketTypeId;
    private String fieldKey, fieldName;
    private CustomFieldType fieldType;
    private String optionsJson;
    private Boolean visible, required;
    private Integer displayOrder;
}
