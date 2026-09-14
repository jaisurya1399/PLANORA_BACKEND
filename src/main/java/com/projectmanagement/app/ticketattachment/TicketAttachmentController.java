package com.projectmanagement.app.ticketattachment;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-attachments")
@RequiredArgsConstructor
public class TicketAttachmentController {

        private final TicketAttachmentService attachmentService;

        /**
         * Get attachments for ticket.
         */
        @GetMapping("/ticket/{ticketId}")
        @PreAuthorize("hasAuthority('ticket_attachment.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketAttachmentResponse>> getByTicketId(
                        @PathVariable @Positive Long ticketId) {

                return ResponseEntity.ok(
                                attachmentService.getByTicketId(ticketId));
        }

        @GetMapping("/comment/{commentId}")
        @PreAuthorize("hasAuthority('ticket_attachment.view') or hasRole('ADMIN')")
        public ResponseEntity<List<TicketAttachmentResponse>> getByCommentId(@PathVariable @Positive Long commentId) {
                return ResponseEntity.ok(attachmentService.getByCommentId(commentId));
        }

        @PostMapping(value = "/comment/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('ticket_attachment.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketAttachmentResponse> uploadToComment(@PathVariable @Positive Long commentId,
                        @RequestPart("file") MultipartFile file) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(attachmentService.uploadToComment(commentId, file));
        }

        /**
         * Upload attachment.
         */
        @PostMapping(value = "/ticket/{ticketId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('ticket_attachment.create') or hasRole('ADMIN')")
        public ResponseEntity<TicketAttachmentResponse> upload(
                        @PathVariable @Positive Long ticketId,
                        @RequestPart("file") MultipartFile file) {

                TicketAttachmentResponse response = attachmentService.upload(
                                ticketId,
                                file);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        /**
         * Download attachment.
         */
        @GetMapping("/{id}/download")
        @PreAuthorize("hasAuthority('ticket_attachment.view') or hasRole('ADMIN')")
        public ResponseEntity<byte[]> download(
                        @PathVariable @Positive Long id) {

                TicketAttachment attachment = attachmentService.getEntity(id);

                MediaType mediaType;

                try {

                        mediaType = MediaType.parseMediaType(
                                        attachment.getContentType());

                } catch (Exception exception) {

                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(mediaType);

                headers.setContentDisposition(
                                ContentDisposition
                                                .attachment()
                                                .filename(
                                                                attachment.getOriginalName())
                                                .build());

                headers.setContentLength(
                                attachment.getFileData().length);

                return new ResponseEntity<>(
                                attachment.getFileData(),
                                headers,
                                HttpStatus.OK);
        }

        /**
         * Delete attachment.
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ticket_attachment.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable @Positive Long id) {

                attachmentService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{id}/view")
        @PreAuthorize("hasAuthority('ticket_attachment.view') or hasRole('ADMIN')")

        public ResponseEntity<byte[]> view(
                        @PathVariable @Positive Long id) {

                TicketAttachment attachment = attachmentService.getEntity(id);

                MediaType mediaType;

                try {
                        mediaType = MediaType.parseMediaType(
                                        attachment.getContentType());
                } catch (Exception exception) {
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(mediaType);

                headers.setContentDisposition(
                                ContentDisposition
                                                .inline()
                                                .filename(attachment.getOriginalName())
                                                .build());

                headers.setContentLength(
                                attachment.getFileSize());

                return new ResponseEntity<>(
                                attachment.getFileData(),
                                headers,
                                HttpStatus.OK);
        }
}