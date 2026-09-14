package com.projectmanagement.app.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadSecurityService {
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> SAFE_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/webp", "image/gif",
            "text/plain", "text/csv", "application/json", "application/zip",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation");

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File is required");
        if (file.getSize() > MAX_FILE_SIZE)
            throw new IllegalArgumentException("File must not exceed 5 MB");
        String type = normalize(file.getContentType());
        if (!SAFE_TYPES.contains(type))
            throw new IllegalArgumentException("Unsupported or unsafe file type");
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".svg") || lower.endsWith(".js")
                    || lower.endsWith(".mjs") || lower.endsWith(".exe") || lower.endsWith(".sh")
                    || lower.endsWith(".bat")
                    || lower.endsWith(".cmd") || lower.endsWith(".jar")) {
                throw new IllegalArgumentException("Executable or browser-active files are not allowed");
            }
        }
        if (IMAGE_TYPES.contains(type)) {
            try {
                if (ImageIO.read(new ByteArrayInputStream(file.getBytes())) == null)
                    throw new IllegalArgumentException("Invalid image file");
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to validate image file", e);
            }
        }
        if ("application/pdf".equals(type)) {
            try {
                byte[] b = file.getBytes();
                if (b.length < 5 || b[0] != '%' || b[1] != 'P' || b[2] != 'D' || b[3] != 'F' || b[4] != '-')
                    throw new IllegalArgumentException("Invalid PDF file");
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to validate PDF file", e);
            }
        }
    }

    public String normalize(String type) {
        return type == null ? "" : type.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }
}
