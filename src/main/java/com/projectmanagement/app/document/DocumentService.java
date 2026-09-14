package com.projectmanagement.app.document;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.security.FileUploadSecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

        private final DocumentRepository documentRepository;
        private final FileUploadSecurityService fileUploadSecurityService;

        @Transactional(readOnly = true)
        public List<DocumentResponse> getAll() {
                return documentRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public DocumentResponse getById(Long id) {
                return toResponse(getEntity(id));
        }

        @Transactional(readOnly = true)
        public List<DocumentResponse> searchByName(String name) {
                return documentRepository
                                .findByNameContainingIgnoreCase(name)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<DocumentResponse> searchByOriginalName(
                        String originalName) {
                return documentRepository
                                .findByOriginalNameContainingIgnoreCase(originalName)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<DocumentResponse> getByContentType(
                        String contentType) {
                return documentRepository
                                .findByContentType(contentType)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        public DocumentResponse create(
                        String name,
                        MultipartFile file) {

                validateFile(file);

                try {
                        Document document = Document.builder()
                                        .name(name)
                                        .originalName(sanitizeName(file.getOriginalFilename()))
                                        .contentType(
                                                        file.getContentType() != null
                                                                        ? file.getContentType()
                                                                        : "application/octet-stream")
                                        .fileSize(file.getSize())
                                        .fileData(file.getBytes())
                                        .build();

                        return toResponse(
                                        documentRepository.save(document));

                } catch (IOException exception) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Unable to read uploaded file",
                                        exception);
                }
        }

        public DocumentResponse update(
                        Long id,
                        String name,
                        MultipartFile file) {

                Document document = getEntity(id);

                document.setName(name);

                if (file != null && !file.isEmpty()) {
                        validateFile(file);
                        try {
                                document.setOriginalName(
                                                sanitizeName(file.getOriginalFilename()));

                                document.setContentType(
                                                file.getContentType() != null
                                                                ? file.getContentType()
                                                                : "application/octet-stream");

                                document.setFileSize(file.getSize());
                                document.setFileData(file.getBytes());

                        } catch (IOException exception) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Unable to read uploaded file",
                                                exception);
                        }
                }

                return toResponse(
                                documentRepository.save(document));
        }

        @Transactional(readOnly = true)
        public byte[] getFileData(Long id) {
                return getEntity(id).getFileData();
        }

        @Transactional(readOnly = true)
        public Document getEntity(Long id) {
                return documentRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Document not found"));
        }

        public void delete(Long id) {
                Document document = getEntity(id);
                documentRepository.delete(document);
        }

        private void validateFile(MultipartFile file) {
                try {
                        fileUploadSecurityService.validate(file);
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
                }
        }

        private String sanitizeName(String name) {
                if (name == null || name.isBlank())
                        return "document";
                String clean = name.replace("\\", "/");
                clean = clean.substring(clean.lastIndexOf('/') + 1);
                return clean.length() > 255 ? clean.substring(0, 255) : clean;
        }

        private DocumentResponse toResponse(Document document) {

                return DocumentResponse.builder()
                                .id(document.getId())
                                .name(document.getName())
                                .originalName(document.getOriginalName())
                                .contentType(document.getContentType())
                                .fileSize(document.getFileSize())
                                .createdAt(document.getCreatedAt())
                                .updatedAt(document.getUpdatedAt())
                                .build();
        }
}