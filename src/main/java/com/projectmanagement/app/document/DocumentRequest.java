package com.projectmanagement.app.document;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private MultipartFile file;
}