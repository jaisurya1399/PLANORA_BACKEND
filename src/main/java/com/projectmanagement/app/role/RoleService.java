package com.projectmanagement.app.role;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + id));

        return toResponse(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse getByName(String name) {

        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + name));

        return toResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getByGuardName(String guardName) {

        return roleRepository.findByGuardName(guardName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse create(RoleRequest request) {

        String guardName = normalizeGuardName(
                request.getGuardName());

        if (roleRepository.existsByNameAndGuardName(
                request.getName(),
                guardName)) {
            throw new RuntimeException(
                    "Role already exists: " + request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .guardName(guardName)
                .build();

        return toResponse(roleRepository.save(role));
    }

    public RoleResponse update(
            Long id,
            RoleRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + id));

        String guardName = normalizeGuardName(
                request.getGuardName());

        roleRepository
                .findByNameAndGuardName(
                        request.getName(),
                        guardName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "Role already exists: "
                                    + request.getName());
                });

        role.setName(request.getName());
        role.setGuardName(guardName);

        return toResponse(roleRepository.save(role));
    }

    public void delete(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + id));

        roleRepository.delete(role);
    }

    private String normalizeGuardName(String guardName) {

        if (guardName == null || guardName.isBlank()) {
            return "web";
        }

        return guardName.trim();
    }

    private RoleResponse toResponse(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .guardName(role.getGuardName())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}