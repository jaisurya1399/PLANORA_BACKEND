package com.projectmanagement.app.release;

import java.time.LocalDate;

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
public class ReleaseVersionRequest {
    @NotBlank
    @Size(max = 100)
    private String version;
    @NotBlank
    @Size(max = 255)
    private String name;
    private String description;
    private String releaseNotes;
    private LocalDate startDate;
    private LocalDate releaseDate;
    private ReleaseStatus status;
}
