package kyung.kung_backend.domain.expert.service;

import kyung.kung_backend.domain.expert.dto.ExpertProfileImageUploadResponse;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertProfileImageService {

    private final ExpertProfileRepository expertProfileRepository;
    private final ExpertProfileImageStorage imageStorage;

    @Transactional
    public ExpertProfileImageUploadResponse uploadProfileImage(
            User user,
            MultipartFile file
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("login is required");
        }

        ExpertProfile expertProfile = expertProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("expert profile not found"));

        ExpertProfileImageStorage.StoredExpertProfileImage storedImage =
                imageStorage.store(file);

        expertProfile.updateExpertProfileImage(
                storedImage.getStoredName(),
                storedImage.getOriginalName(),
                storedImage.getContentType(),
                storedImage.getFileSize()
        );

        return ExpertProfileImageUploadResponse.builder()
                .expertProfileId(expertProfile.getExpertProfileId())
                .originalName(storedImage.getOriginalName())
                .storedName(storedImage.getStoredName())
                .expertProfileImageUrl(storedImage.getImageUrl())
                .contentType(storedImage.getContentType())
                .fileSize(storedImage.getFileSize())
                .build();
    }
}
