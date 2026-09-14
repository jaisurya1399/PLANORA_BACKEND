package com.projectmanagement.app.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetConfirmRequest {
    @NotBlank
    private String token;
    @NotBlank
    @Size(min = 12, max = 255)
    private String newPassword;
}
