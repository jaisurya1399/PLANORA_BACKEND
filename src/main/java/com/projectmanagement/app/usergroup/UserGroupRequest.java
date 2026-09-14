package com.projectmanagement.app.usergroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    @Size(max = 500)
    private String description;
}
