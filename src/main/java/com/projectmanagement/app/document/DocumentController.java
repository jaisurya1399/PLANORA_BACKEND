package com.projectmanagement.app.document;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

        private final DocumentService documentService;

        @GetMapping
        @PreAuthorize("hasAuthority('document.view') or hasRole('ADMIN')")
        public ResponseEntity<List<DocumentResponse>> getAll() {
                return ResponseEntity.ok(
                                documentService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('document.view') or hasRole('ADMIN')")
        public ResponseEntity<DocumentResponse> getById(
                        @PathVariable @Positive Long id) {
                return ResponseEntity.ok(
                                documentService.getById(id));
        }

        @GetMapping("/search")
        @PreAuthorize("hasAuthority('document.view') or hasRole('ADMIN')")
        public ResponseEntity<List<DocumentResponse>> search(
                        @RequestParam @NotBlank String name) {
                return ResponseEntity.ok(
                                documentService.searchByName(name));
        }

        @GetMapping("/content-type")
        @PreAuthorize("hasAuthority('document.view') or hasRole('ADMIN')")
        public ResponseEntity<List<DocumentResponse>> getByContentType(
                        @RequestParam String contentType) {
                return ResponseEntity.ok(
                                documentService.getByContentType(contentType));
        }

        @GetMapping("/{id}/download")
        @PreAuthorize("hasAuthority('document.view') or hasRole('ADMIN')")
        public ResponseEntity<byte[]> download(
                        @PathVariable @Positive Long id) {

                Document document = documentService.getEntity(id);

                MediaType mediaType;

                try {
                        mediaType = MediaType.parseMediaType(
                                        document.getContentType());
                } catch (Exception exception) {
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(mediaType);

                headers.setContentDisposition(
                                ContentDisposition.attachment()
                                                .filename(document.getOriginalName())
                                                .build());

                headers.setContentLength(
                                document.getFileSize());

                return new ResponseEntity<>(
                                document.getFileData(),
                                headers,
                                HttpStatus.OK);
        }

        /**
         * View document inline in the browser.
         *
         * Used by the UI eye/preview action. The download endpoint intentionally
         * keeps Content-Disposition=attachment; this endpoint uses inline so
         * browser-supported files such as PDF and images can be previewed.
         */
        @GetMapping("/{id}/view")
        @PreAuthorize("hasAuthority('document.view') or hasRole('ADMIN')")
        public ResponseEntity<byte[]> view(
                        @PathVariable @Positive Long id) {

                Document document = documentService.getEntity(id);

                MediaType mediaType;

                try {
                        mediaType = MediaType.parseMediaType(
                                        document.getContentType());
                } catch (Exception exception) {
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(mediaType);
                headers.setContentDisposition(
                                ContentDisposition.inline()
                                                .filename(document.getOriginalName())
                                                .build());
                headers.setContentLength(document.getFileSize());

                return new ResponseEntity<>(
                                document.getFileData(),
                                headers,
                                HttpStatus.OK);
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('document.create') or hasRole('ADMIN')")
        public ResponseEntity<DocumentResponse> create(
                        @RequestParam @NotBlank String name,
                        @RequestPart("file") MultipartFile file) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                documentService.create(name, file));
        }

        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('document.update') or hasRole('ADMIN')")
        public ResponseEntity<DocumentResponse> update(
                        @PathVariable @Positive Long id,
                        @RequestParam @NotBlank String name,
                        @RequestPart(value = "file", required = false) MultipartFile file) {

                return ResponseEntity.ok(
                                documentService.update(
                                                id,
                                                name,
                                                file));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('document.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> delete(
                        @PathVariable @Positive Long id) {

                documentService.delete(id);

                return ResponseEntity.noContent().build();
        }
}