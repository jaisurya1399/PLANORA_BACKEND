package com.projectmanagement.app.metadata;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/application-metadata")
public class ApplicationMetadataController {

        private final ApplicationMetadataService metadataService;

        public ApplicationMetadataController(
                        ApplicationMetadataService metadataService) {

                this.metadataService = metadataService;
        }

        @GetMapping
        @PreAuthorize("hasAuthority('metadata.view') or hasRole('ADMIN')")
        public ResponseEntity<List<ApplicationMetadata>> getAllMetadata() {

                return ResponseEntity.ok(
                                metadataService.getAllMetadata());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('metadata.view') or hasRole('ADMIN')")
        public ResponseEntity<ApplicationMetadata> getMetadataById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                metadataService.getMetadataById(id));
        }

        @GetMapping("/key/{key}")
        @PreAuthorize("hasAuthority('metadata.view') or hasRole('ADMIN')")
        public ResponseEntity<ApplicationMetadata> getMetadataByKey(
                        @PathVariable String key) {

                return ResponseEntity.ok(
                                metadataService.getMetadataByKey(key));
        }

        @GetMapping("/exists/key/{key}")
        @PreAuthorize("hasAuthority('metadata.view') or hasRole('ADMIN')")
        public ResponseEntity<Boolean> existsByKey(
                        @PathVariable String key) {

                return ResponseEntity.ok(
                                metadataService.existsByKey(key));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('metadata.create') or hasRole('ADMIN')")
        public ResponseEntity<ApplicationMetadata> createMetadata(
                        @Valid @RequestBody ApplicationMetadataRequest request) {

                ApplicationMetadata createdMetadata = metadataService.createMetadata(
                                request.getKey(),
                                request.getValue(),
                                request.getDescription());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(createdMetadata);
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('metadata.update') or hasRole('ADMIN')")
        public ResponseEntity<ApplicationMetadata> updateMetadata(
                        @PathVariable Long id,
                        @Valid @RequestBody ApplicationMetadataRequest request) {

                ApplicationMetadata updatedMetadata = metadataService.updateMetadata(
                                id,
                                request.getKey(),
                                request.getValue(),
                                request.getDescription());

                return ResponseEntity.ok(updatedMetadata);
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('metadata.delete') or hasRole('ADMIN')")
        public ResponseEntity<Void> deleteMetadata(
                        @PathVariable Long id) {

                metadataService.deleteMetadata(id);

                return ResponseEntity.noContent().build();
        }
}