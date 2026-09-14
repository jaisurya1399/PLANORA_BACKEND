package com.projectmanagement.app.metadata;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationMetadataService {

    private final ApplicationMetadataRepository metadataRepository;

    public ApplicationMetadataService(
            ApplicationMetadataRepository metadataRepository) {

        this.metadataRepository = metadataRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationMetadata> getAllMetadata() {

        return metadataRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ApplicationMetadata getMetadataById(Long id) {

        validateId(id);

        return metadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Application metadata not found with id: "
                                + id));
    }

    @Transactional(readOnly = true)
    public ApplicationMetadata getMetadataByKey(String key) {

        validateKey(key);

        String normalizedKey = normalizeKey(key);

        return metadataRepository.findByKey(normalizedKey)
                .orElseThrow(() -> new RuntimeException(
                        "Application metadata not found with key: "
                                + normalizedKey));
    }

    @Transactional(readOnly = true)
    public boolean existsByKey(String key) {

        validateKey(key);

        String normalizedKey = normalizeKey(key);

        return metadataRepository.existsByKey(normalizedKey);
    }

    public ApplicationMetadata createMetadata(
            String key,
            String value,
            String description) {

        validateKey(key);

        String normalizedKey = normalizeKey(key);

        if (metadataRepository.existsByKey(normalizedKey)) {
            throw new RuntimeException(
                    "Application metadata already exists with key: "
                            + normalizedKey);
        }

        ApplicationMetadata metadata = ApplicationMetadata.builder()
                .key(normalizedKey)
                .value(value)
                .description(description)
                .build();

        return metadataRepository.save(metadata);
    }

    public ApplicationMetadata updateMetadata(
            Long id,
            String key,
            String value,
            String description) {

        ApplicationMetadata existingMetadata = getMetadataById(id);

        if (key != null && !key.isBlank()) {

            String normalizedKey = normalizeKey(key);

            if (!normalizedKey.equalsIgnoreCase(
                    existingMetadata.getKey())) {

                if (metadataRepository.existsByKey(
                        normalizedKey)) {

                    throw new RuntimeException(
                            "Application metadata already exists "
                                    + "with key: "
                                    + normalizedKey);
                }

                existingMetadata.setKey(normalizedKey);
            }
        }

        if (value != null) {
            existingMetadata.setValue(value);
        }

        if (description != null) {
            existingMetadata.setDescription(description);
        }

        return metadataRepository.save(existingMetadata);
    }

    public void deleteMetadata(Long id) {

        ApplicationMetadata metadata = getMetadataById(id);

        metadataRepository.delete(metadata);
    }

    public void deleteMetadataByKey(String key) {

        validateKey(key);

        String normalizedKey = normalizeKey(key);

        if (!metadataRepository.existsByKey(normalizedKey)) {
            throw new RuntimeException(
                    "Application metadata not found with key: "
                            + normalizedKey);
        }

        metadataRepository.deleteByKey(normalizedKey);
    }

    private void validateKey(String key) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Metadata key cannot be empty");
        }
    }

    private void validateId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Metadata id must be a valid positive number");
        }
    }

    private String normalizeKey(String key) {

        return key.trim();
    }
}