package com.projectmanagement.app.project;

import jakarta.validation.constraints.NotBlank;
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
public class ProjectStatusRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String color;

    private Boolean isDefault;
}