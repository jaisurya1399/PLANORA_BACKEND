package com.projectmanagement.app.customfield;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomFieldRequest {
    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9_\\-]*", message = "Key must start with a letter and contain only letters, numbers, _ or -")
    private String key;
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotNull
    private CustomFieldType type;
    private String description;
    private String optionsJson;
    private Boolean requiredByDefault;
    private Boolean active;
}
