package com.projectmanagement.app.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelRequest {

    @NotBlank(message = "Label name is required")
    @Size(max = 100, message = "Label name cannot exceed 100 characters")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid HEX color like #1976D2")
    private String color;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}