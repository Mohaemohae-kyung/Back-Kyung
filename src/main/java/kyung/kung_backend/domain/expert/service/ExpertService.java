package kyung.kung_backend.domain.expert.service;

import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpertService {

    private final ExpertProfileRepository expertProfileRepository;

    public void createProfile(User user, ExpertProfileCreateRequest request) {

        if (expertProfileRepository.existsByUser(user)) {
            throw new IllegalArgumentException("이미 고수 프로필이 존재합니다.");
        }

        ExpertProfile expertProfile = new ExpertProfile(
                user,
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears()
        );

        expertProfileRepository.save(expertProfile);
    }
}