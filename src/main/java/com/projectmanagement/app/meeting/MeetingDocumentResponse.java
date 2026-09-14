package com.projectmanagement.app.meeting;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDocumentResponse {
    private Long id, documentId;
    private String name, originalName, contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
