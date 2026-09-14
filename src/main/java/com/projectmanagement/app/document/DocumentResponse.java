package com.projectmanagement.app.document;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentResponse {

    private Long id;

    private String name;

    private String originalName;

    private String contentType;

    private Long fileSize;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}