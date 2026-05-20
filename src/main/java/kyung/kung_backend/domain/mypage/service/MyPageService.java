package kyung.kung_backend.domain.mypage.service;

import kyung.kung_backend.domain.community.repository.CommunityPostRepository;
import kyung.kung_backend.domain.favorite.repository.FavoriteExpertRepository;
import kyung.kung_backend.domain.mypage.dto.MyPageSummaryResponse;
import kyung.kung_backend.domain.request.enums.RequestStatus;
import kyung.kung_backend.domain.request.repository.ServiceRequestRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private static final String COMMUNITY_POST_ACTIVE_STATUS = "ACTIVE";

    private final UserService userService;
    private final FavoriteExpertRepository favoriteExpertRepository;
    private final CommunityPostRepository communityPostRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public MyPageSummaryResponse getMyPageSummary(User currentUser) {
        User user = userService.getUser(currentUser.getUserId());

        long inProgressCount = serviceRequestRepository.countByUserAndStatusInAndDeletedAtIsNull(
                user,
                List.of(RequestStatus.PENDING, RequestStatus.CHATTING)
        );

        long bookmarkExpertCount = favoriteExpertRepository.countByUser(user);

        long postCount = communityPostRepository.countByUserAndStatus(
                user,
                COMMUNITY_POST_ACTIVE_STATUS
        );

        return MyPageSummaryResponse.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .inProgressCount(inProgressCount)
                .bookmarkExpertCount(bookmarkExpertCount)
                .postCount(postCount)
                .build();
    }
}