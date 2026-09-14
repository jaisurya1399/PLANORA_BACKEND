package com.projectmanagement.app.ticketattachment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.security.FileUploadSecurityService;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketComment;
import com.projectmanagement.app.ticket.TicketCommentRepository;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketAttachmentService {

        private final TicketAttachmentRepository attachmentRepository;
        private final TicketRepository ticketRepository;
        private final TicketCommentRepository commentRepository;
        private final ProjectAccessService projectAccessService;
        private final FileUploadSecurityService fileUploadSecurityService;

        /**
         * Get all attachments for a ticket.
         */
        @Transactional(readOnly = true)
        public List<TicketAttachmentResponse> getByTicketId(Long ticketId) {

                if (ticketId == null || ticketId <= 0) {
                        throw new IllegalArgumentException("Invalid ticket ID");
                }

                if (!ticketRepository.existsById(ticketId)) {
                        throw new RuntimeException(
                                        "Ticket not found with id: " + ticketId);
                }

                return attachmentRepository
                                .findByTicketIdOrderByCreatedAtDesc(ticketId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /**
         * Get attachment entity by ID.
         */
        @Transactional(readOnly = true)
        public TicketAttachment getEntity(Long id) {

                if (id == null || id <= 0) {
                        throw new IllegalArgumentException("Invalid attachment ID");
                }

                return attachmentRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Attachment not found with id: " + id));
        }

        /**
         * Upload attachment to a ticket.
         */
        @Transactional
        public TicketAttachmentResponse upload(
                        Long ticketId,
                        MultipartFile file) {

                if (ticketId == null || ticketId <= 0) {
                        throw new IllegalArgumentException("Invalid ticket ID");
                }

                validateFile(file);

                Ticket ticket = ticketRepository
                                .findById(ticketId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with id: " + ticketId));

                try {

                        String originalName = sanitizeName(
                                        file.getOriginalFilename());

                        String contentType = file.getContentType();

                        if (contentType == null || contentType.isBlank()) {
                                contentType = "application/octet-stream";
                        }

                        byte[] fileData = file.getBytes();

                        TicketAttachment attachment = TicketAttachment.builder()
                                        .ticket(ticket)
                                        .fileName(originalName)
                                        .originalName(originalName)
                                        .contentType(contentType)
                                        .fileSize((long) fileData.length)
                                        .fileData(fileData)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        TicketAttachment saved = attachmentRepository.save(attachment);

                        return toResponse(saved);

                } catch (Exception exception) {

                        throw new RuntimeException(
                                        "Failed to upload attachment: "
                                                        + exception.getMessage(),
                                        exception);
                }
        }

        /**
         * Get all attachments for a comment.
         */
        @Transactional(readOnly = true)
        public List<TicketAttachmentResponse> getByCommentId(
                        Long commentId) {

                if (commentId == null || commentId <= 0) {
                        throw new IllegalArgumentException(
                                        "Invalid comment ID");
                }

                TicketComment comment = commentRepository
                                .findById(commentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Comment not found with id: " + commentId));

                projectAccessService.requireView(
                                comment.getTicket().getProject());

                return attachmentRepository
                                .findByComment_IdOrderByCreatedAtDesc(commentId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /**
         * Upload attachment to a comment.
         */
        @Transactional
        public TicketAttachmentResponse uploadToComment(
                        Long commentId,
                        MultipartFile file) {

                if (commentId == null || commentId <= 0) {
                        throw new IllegalArgumentException(
                                        "Invalid comment ID");
                }

                TicketComment comment = commentRepository
                                .findById(commentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Comment not found with id: " + commentId));

                projectAccessService.requireEditor(
                                comment.getTicket().getProject());

                validateFile(file);

                try {

                        String originalName = sanitizeName(
                                        file.getOriginalFilename());

                        String contentType = file.getContentType();

                        if (contentType == null || contentType.isBlank()) {
                                contentType = "application/octet-stream";
                        }

                        byte[] fileData = file.getBytes();

                        TicketAttachment attachment = TicketAttachment.builder()
                                        .ticket(comment.getTicket())
                                        .comment(comment)
                                        .fileName(originalName)
                                        .originalName(originalName)
                                        .contentType(contentType)
                                        .fileSize((long) fileData.length)
                                        .fileData(fileData)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        return toResponse(
                                        attachmentRepository.save(attachment));

                } catch (Exception exception) {

                        throw new RuntimeException(
                                        "Failed to upload comment attachment: "
                                                        + exception.getMessage(),
                                        exception);
                }
        }

        /**
         * Validate uploaded file.
         */
        private void validateFile(MultipartFile file) {
                fileUploadSecurityService.validate(file);
        }

        /**
         * Sanitize original filename.
         *
         * Prevents path traversal such as:
         * ../../file.txt
         */
        private String sanitizeName(String name) {

                if (name == null || name.isBlank()) {
                        return "attachment";
                }

                String clean = name.replace("\\", "/");

                String safe = clean.substring(clean.lastIndexOf('/') + 1);
                if (safe.length() > 255)
                        safe = safe.substring(0, 255);
                return safe;
        }

        /**
         * Delete attachment.
         */
        @Transactional
        public void delete(Long id) {

                if (id == null || id <= 0) {
                        throw new IllegalArgumentException(
                                        "Invalid attachment ID");
                }

                if (!attachmentRepository.existsById(id)) {
                        throw new RuntimeException(
                                        "Attachment not found with id: " + id);
                }

                attachmentRepository.deleteById(id);
        }

        /**
         * Convert entity to response DTO.
         */
        private TicketAttachmentResponse toResponse(
                        TicketAttachment attachment) {

                return TicketAttachmentResponse.builder()
                                .id(attachment.getId())
                                .ticketId(
                                                attachment.getTicket().getId())
                                .commentId(
                                                attachment.getComment() != null
                                                                ? attachment.getComment().getId()
                                                                : null)
                                .fileName(
                                                attachment.getFileName())
                                .originalName(
                                                attachment.getOriginalName())
                                .contentType(
                                                attachment.getContentType())
                                .fileSize(
                                                attachment.getFileSize())
                                .createdAt(
                                                attachment.getCreatedAt())
                                .updatedAt(
                                                attachment.getUpdatedAt())
                                .build();
        }
}