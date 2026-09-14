package com.projectmanagement.app.setting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettingRequest {

    @NotBlank(message = "Group is required")
    @Size(max = 255, message = "Group cannot exceed 255 characters")
    private String group;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    private Boolean locked = false;

    private String payload;
}