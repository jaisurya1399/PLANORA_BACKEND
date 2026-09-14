package com.projectmanagement.app.reminder;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReminderRequest {
    @NotBlank
    @Size(max = 255)
    private String title;
    @Size(max = 5000)
    private String description;
    @NotNull
    @FutureOrPresent
    private LocalDateTime remindAt;
}
