package com.projectmanagement.app.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projectmanagement.app.auth.PasswordPolicy;
import com.projectmanagement.app.security.FileUploadSecurityService;

@Service
public class UserService {
    private static final long MAX_PROFILE_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final FileUploadSecurityService fileUploadSecurityService;

    public UserService(UserRepository r, PasswordEncoder p, PasswordPolicy policy,
            FileUploadSecurityService fileSecurity) {
        userRepository = r;
        passwordEncoder = p;
        passwordPolicy = policy;
        fileUploadSecurityService = fileSecurity;
    }

    public List<User> getAllUsers() {
        return userRepository.findActiveUsers();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already exists: " + request.getEmail());
        passwordPolicy.validate(request.getPassword());
        return userRepository.save(User.builder().name(request.getName()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())).build());
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        User u = getUserById(id);
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(u.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new RuntimeException("Email already exists: " + request.getEmail());
            u.setEmail(request.getEmail());
        }
        if (request.getName() != null && !request.getName().isBlank())
            u.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordPolicy.validate(request.getPassword());
            u.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepository.save(u);
    }

    public void deleteUser(Long id) {
        User u = getUserById(id);
        u.setDeletedAt(LocalDateTime.now());
        userRepository.save(u);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User uploadProfileImage(Long id, MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Profile image is required");
        fileUploadSecurityService.validate(file);
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE)
            throw new IllegalArgumentException("Profile image must not exceed 5 MB");
        String type = file.getContentType();
        if (type == null || !ALLOWED.contains(type.toLowerCase()))
            throw new IllegalArgumentException("Only JPG, PNG, WEBP and GIF images are supported");
        try {
            User u = getUserById(id);
            u.setProfileImageData(file.getBytes());
            u.setProfileImageContentType(type);
            u.setProfileImageFileName(sanitizeFileName(file.getOriginalFilename()));
            return userRepository.save(u);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store profile image", e);
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank())
            return "profile";
        String clean = name.replace("\\", "/");
        clean = clean.substring(clean.lastIndexOf('/') + 1);
        clean = clean.replaceAll("[^A-Za-z0-9._-]", "_");
        return clean.length() > 120 ? clean.substring(0, 120) : clean;
    }

    @Transactional
    public void deleteProfileImage(Long id) {
        User u = getUserById(id);
        u.setProfileImageData(null);
        u.setProfileImageContentType(null);
        u.setProfileImageFileName(null);
        userRepository.save(u);
    }
}
