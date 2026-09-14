package com.projectmanagement.app.setting;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingService {

    private final SettingRepository settingRepository;

    @Transactional(readOnly = true)
    public List<SettingResponse> getAll() {
        return settingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettingResponse getById(Long id) {

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        return toResponse(setting);
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> getByGroup(String group) {

        return settingRepository
                .findByGroupOrderByNameAsc(group)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettingResponse getByGroupAndName(
            String group,
            String name) {

        Setting setting = settingRepository
                .findByGroupAndName(group, name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        return toResponse(setting);
    }

    @Transactional(readOnly = true)
    public SettingResponse getByName(String name) {

        Setting setting = settingRepository
                .findByName(name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        return toResponse(setting);
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> getByLocked(Boolean locked) {

        return settingRepository
                .findByLocked(locked)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> getByGroupAndLocked(
            String group,
            Boolean locked) {

        return settingRepository
                .findByGroupAndLocked(group, locked)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SettingResponse create(SettingRequest request) {

        if (settingRepository.existsByGroupAndName(
                request.getGroup(),
                request.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Setting already exists in this group");
        }

        Setting setting = Setting.builder()
                .group(request.getGroup())
                .name(request.getName())
                .locked(
                        request.getLocked() != null
                                ? request.getLocked()
                                : false)
                .payload(request.getPayload())
                .build();

        return toResponse(
                settingRepository.save(setting));
    }

    public SettingResponse update(
            Long id,
            SettingRequest request) {

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        /*
         * Locked settings cannot be modified.
         */
        if (Boolean.TRUE.equals(setting.getLocked())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Locked setting cannot be modified");
        }

        boolean duplicate = settingRepository
                .findByGroupAndName(
                        request.getGroup(),
                        request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Setting already exists in this group");
        }

        setting.setGroup(request.getGroup());
        setting.setName(request.getName());
        setting.setLocked(
                request.getLocked() != null
                        ? request.getLocked()
                        : false);
        setting.setPayload(request.getPayload());

        return toResponse(
                settingRepository.save(setting));
    }

    public void delete(Long id) {

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        if (Boolean.TRUE.equals(setting.getLocked())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Locked setting cannot be deleted");
        }

        settingRepository.delete(setting);
    }

    public SettingResponse lock(Long id) {

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        setting.setLocked(true);

        return toResponse(
                settingRepository.save(setting));
    }

    public SettingResponse unlock(Long id) {

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setting not found"));

        setting.setLocked(false);

        return toResponse(
                settingRepository.save(setting));
    }

    @Transactional(readOnly = true)
    public long countByGroup(String group) {
        return settingRepository.countByGroup(group);
    }

    @Transactional(readOnly = true)
    public long countByLocked(Boolean locked) {
        return settingRepository.countByLocked(locked);
    }

    private SettingResponse toResponse(Setting setting) {

        return SettingResponse.builder()
                .id(setting.getId())
                .group(setting.getGroup())
                .name(setting.getName())
                .locked(setting.getLocked())
                .payload(setting.getPayload())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}