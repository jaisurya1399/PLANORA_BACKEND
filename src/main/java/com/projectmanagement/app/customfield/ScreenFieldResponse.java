package com.projectmanagement.app.customfield;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenFieldResponse {
    private Long id, fieldId;
    private String fieldKey, fieldName;
    private CustomFieldType fieldType;
    private String optionsJson;
    private Integer displayOrder;
    private Boolean visible;
}
