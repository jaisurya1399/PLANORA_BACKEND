package com.projectmanagement.app.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.projectmanagement.app.auth.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService s, CurrentUserService c) {
        userService = s;
        currentUserService = c;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user.view') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.view') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(userService.getUserById(id)));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('user.view') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(toResponse(userService.getUserByEmail(email)));
    }

    @GetMapping("/exists/email/{email}")
    @PreAuthorize("hasAuthority('user.view') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.existsByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.create') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(userService.createUser(r)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest r) {
        return ResponseEntity.ok(toResponse(userService.updateUser(id, r)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user.update') or hasRole('ADMIN') or @currentUserService.getCurrentUserId() == #id")
    public ResponseEntity<UserResponse> uploadProfileImage(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(toResponse(userService.uploadProfileImage(id, file)));
    }

    @GetMapping("/{id}/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        User u = userService.getUserById(id);
        if (u.getProfileImageData() == null || u.getProfileImageData().length == 0)
            return ResponseEntity.notFound().build();
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (u.getProfileImageContentType() != null)
                mt = MediaType.parseMediaType(u.getProfileImageContentType());
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok().contentType(mt).body(u.getProfileImageData());
    }

    @DeleteMapping("/{id}/profile-image")
    @PreAuthorize("hasAuthority('user.update') or hasRole('ADMIN') or @currentUserService.getCurrentUserId() == #id")
    public ResponseEntity<Void> deleteProfileImage(@PathVariable Long id) {
        userService.deleteProfileImage(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder().id(u.getId()).name(u.getName()).email(u.getEmail())
                .emailVerifiedAt(u.getEmailVerifiedAt()).createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt())
                .hasProfileImage(u.getProfileImageData() != null && u.getProfileImageData().length > 0).build();
    }
}
