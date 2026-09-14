package com.projectmanagement.app.ticketattachment;

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
public class TicketAttachmentResponse {

    private Long id;

    private Long ticketId;

    private Long commentId;

    private String fileName;

    private String originalName;

    private String contentType;

    private Long fileSize;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}