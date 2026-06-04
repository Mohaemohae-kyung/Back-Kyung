package kyung.kung_backend.domain.expert.service;

import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
public class ExpertProfileImageStorage {

    private static final long MAX_PROFILE_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "webp",
            "svg",
            "jsp"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            "application/octet-stream"
    );

    private final Path uploadRoot;
    private final String publicBaseUrl;

    public ExpertProfileImageStorage(
            @Value("${expert-profile.image.upload-dir}")
            String uploadDir,
            @Value("${expert-profile.image.public-base-url}")
            String publicBaseUrl
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    public StoredExpertProfileImage store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("profile image file is empty");
        }

        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "profile-image");
        String extension = validateProfileImage(file, originalName);
        String storedName = createSafeStoredName(extension);

        Path targetPath = uploadRoot.resolve(storedName).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("invalid profile image path");
        }

        try {
            Path parent = targetPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to store profile image", e);
        }

        return StoredExpertProfileImage.builder()
                .originalName(originalName)
                .storedName(storedName)
                .imageUrl(buildPublicUrl(storedName))
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }

    public String buildPublicUrl(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return null;
        }

        return publicBaseUrl + "/" + storedName.replace("\\", "/");
    }

    private String validateProfileImage(
            MultipartFile file,
            String originalName
    ) {
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("profile image file is too large");
        }

        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("unsupported profile image extension");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("unsupported profile image content type");
        }

        return extension;
    }

    private String createSafeStoredName(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private String extractExtension(String originalName) {
        if (originalName == null) {
            return "";
        }

        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }

        return originalName.substring(index + 1)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        int parameterStart = contentType.indexOf(';');
        String normalized = parameterStart >= 0
                ? contentType.substring(0, parameterStart)
                : contentType;

        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed;
    }

    @Getter
    @Builder
    public static class StoredExpertProfileImage {

        private String originalName;

        private String storedName;

        private String imageUrl;

        private String contentType;

        private Long fileSize;
    }
}
