package kyung.kung_backend.domain.user.service;

import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void deleteMyAccount(User user) {
        User findUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        findUser.delete();
    }
}