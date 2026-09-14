package com.projectmanagement.app.customfield;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomFieldResponse {
    private Long id;
    private String key;
    private String name;
    private CustomFieldType type;
    private String description;
    private String optionsJson;
    private Boolean requiredByDefault;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
