package com.projectmanagement.app.customfield;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomFieldService {
    private final CustomFieldRepository repository;

    @Transactional(readOnly = true)
    public List<CustomFieldResponse> getAll() {
        return repository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomFieldResponse> getActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomFieldResponse get(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() -> notFound("Custom field not found")));
    }

    public CustomFieldResponse create(CustomFieldRequest request) {
        String key = normalizeKey(request.getKey());
        if (repository.existsByKey(key))
            throw conflict("Custom field key already exists: " + key);
        CustomField field = CustomField.builder().key(key).name(request.getName().trim()).type(request.getType())
                .description(request.getDescription()).optionsJson(request.getOptionsJson())
                .requiredByDefault(Boolean.TRUE.equals(request.getRequiredByDefault()))
                .active(request.getActive() == null || request.getActive()).build();
        validateOptions(field);
        return toResponse(repository.save(field));
    }

    public CustomFieldResponse update(Long id, CustomFieldRequest request) {
        CustomField field = repository.findById(id).orElseThrow(() -> notFound("Custom field not found"));
        String key = normalizeKey(request.getKey());
        repository.findByKey(key).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
            throw conflict("Custom field key already exists: " + key);
        });
        field.setKey(key);
        field.setName(request.getName().trim());
        field.setType(request.getType());
        field.setDescription(request.getDescription());
        field.setOptionsJson(request.getOptionsJson());
        field.setRequiredByDefault(Boolean.TRUE.equals(request.getRequiredByDefault()));
        field.setActive(request.getActive() == null || request.getActive());
        validateOptions(field);
        return toResponse(repository.save(field));
    }

    public void delete(Long id) {
        CustomField field = repository.findById(id).orElseThrow(() -> notFound("Custom field not found"));
        field.setActive(false);
        repository.save(field);
    }

    private void validateOptions(CustomField field) {
        if ((field.getType() == CustomFieldType.SELECT || field.getType() == CustomFieldType.MULTI_SELECT)
                && (field.getOptionsJson() == null || field.getOptionsJson().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select fields require optionsJson");
    }

    private String normalizeKey(String key) {
        return key.trim().toLowerCase();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private CustomFieldResponse toResponse(CustomField f) {
        return CustomFieldResponse.builder().id(f.getId()).key(f.getKey()).name(f.getName()).type(f.getType())
                .description(f.getDescription()).optionsJson(f.getOptionsJson())
                .requiredByDefault(f.getRequiredByDefault()).active(f.getActive()).createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt()).build();
    }
}
