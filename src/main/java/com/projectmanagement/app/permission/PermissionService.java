package com.projectmanagement.app.permission;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    // =========================
    // GET ALL PERMISSIONS
    // =========================
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // =========================
    // GET PERMISSION BY ID
    // =========================
    @Transactional(readOnly = true)
    public Permission getPermissionById(Long id) {

        validateId(id);

        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Permission not found with id: " + id));
    }

    // =========================
    // GET PERMISSION BY NAME
    // =========================
    @Transactional(readOnly = true)
    public Permission getPermissionByName(String name) {

        String normalizedName = normalizeName(name);

        validateName(normalizedName);

        return permissionRepository.findByName(normalizedName)
                .orElseThrow(() -> new RuntimeException(
                        "Permission not found with name: "
                                + normalizedName));
    }

    // =========================
    // CHECK PERMISSION EXISTS
    // =========================
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {

        String normalizedName = normalizeName(name);

        validateName(normalizedName);

        return permissionRepository.existsByName(normalizedName);
    }

    // =========================
    // CREATE PERMISSION
    // =========================
    public Permission createPermission(Permission request) {

        validateRequest(request);

        String name = normalizeName(request.getName());
        String guardName = normalizeGuardName(request.getGuardName());

        validateName(name);
        validateGuardName(guardName);

        if (permissionRepository.existsByNameAndGuardName(
                name,
                guardName)) {

            throw new RuntimeException(
                    "Permission already exists with name: "
                            + name
                            + " and guard name: "
                            + guardName);
        }

        Permission permission = new Permission();

        permission.setName(name);
        permission.setGuardName(guardName);

        return permissionRepository.save(permission);
    }

    // =========================
    // UPDATE PERMISSION
    // =========================
    public Permission updatePermission(
            Long id,
            Permission request) {

        validateId(id);
        validateRequest(request);

        Permission existingPermission = getPermissionById(id);

        String newName = existingPermission.getName();
        String newGuardName = existingPermission.getGuardName();

        // -------------------------
        // UPDATE NAME
        // -------------------------
        if (request.getName() != null) {

            newName = normalizeName(request.getName());

            validateName(newName);
        }

        // -------------------------
        // UPDATE GUARD NAME
        // -------------------------
        if (request.getGuardName() != null) {

            newGuardName = normalizeGuardName(
                    request.getGuardName());

            validateGuardName(newGuardName);
        }

        // -------------------------
        // CHECK DUPLICATE
        // -------------------------
        if (!newName.equals(existingPermission.getName())
                || !newGuardName.equals(existingPermission.getGuardName())) {

            if (permissionRepository
                    .existsByNameAndGuardNameAndIdNot(
                            newName,
                            newGuardName,
                            id)) {

                throw new RuntimeException(
                        "Permission already exists with name: "
                                + newName
                                + " and guard name: "
                                + newGuardName);
            }
        }

        existingPermission.setName(newName);
        existingPermission.setGuardName(newGuardName);

        return permissionRepository.save(existingPermission);
    }

    // =========================
    // DELETE PERMISSION
    // =========================
    public void deletePermission(Long id) {

        validateId(id);

        Permission permission = getPermissionById(id);

        permissionRepository.delete(permission);
    }

    // =========================
    // VALIDATE REQUEST
    // =========================
    private void validateRequest(Permission request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Permission request cannot be null");
        }
    }

    // =========================
    // VALIDATE NAME
    // =========================
    private void validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission name is required");
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Permission name cannot exceed 255 characters");
        }
    }

    // =========================
    // VALIDATE GUARD NAME
    // =========================
    private void validateGuardName(String guardName) {

        if (guardName == null || guardName.isBlank()) {
            throw new IllegalArgumentException(
                    "Guard name is required");
        }

        if (guardName.length() > 255) {
            throw new IllegalArgumentException(
                    "Guard name cannot exceed 255 characters");
        }
    }

    // =========================
    // NORMALIZE NAME
    // =========================
    private String normalizeName(String name) {

        if (name == null) {
            return null;
        }

        return name.trim();
    }

    // =========================
    // NORMALIZE GUARD NAME
    // =========================
    private String normalizeGuardName(String guardName) {

        if (guardName == null) {
            return null;
        }

        return guardName.trim();
    }

    // =========================
    // VALIDATE ID
    // =========================
    private void validateId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Permission ID must be a valid positive number");
        }
    }
}