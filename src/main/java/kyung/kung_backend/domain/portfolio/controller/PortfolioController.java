package kyung.kung_backend.domain.portfolio.controller;

import kyung.kung_backend.domain.portfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kyung.kung_backend.domain.portfolio.dto.PortfolioSyncRequest;
import java.util.Map;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/api/portfolios/viewer")
    public ResponseEntity<byte[]> getPortfolioView(@RequestParam String url) {
        return portfolioService.fetchPortfolioWithSsrf(url);
    }

    /**
     * @deprecated [구버전 레거시] 외부 플랫폼 포트폴리오 동기화 API
     * <p>주의: 현재는 통합 프로필 수정 API(@PatchMapping("/profile")) 기능으로 대체되어
     * 프론트엔드 정적 UI 메뉴에서는 접근 경로가 완전히 제거되었습니다.
     * 다만, 과거 연동 이력 및 하위 호환성을 위해 백엔드 컨트롤러에 남깁니다.</p>
     * * @param request 외부 포트폴리오 동기화 요청 객체 (targetUrl 포함)
     * @param headers 외부 플랫폼 인증에 사용되던 헤더 정보
     */
    @Deprecated
    @PutMapping("/api/portfolios/sync")
    public ResponseEntity<String> syncExternalPortfolio(
            @RequestBody PortfolioSyncRequest request,
            @RequestHeader Map<String, String> headers) {

        return portfolioService.syncWithExternalPlatform(request.getTargetUrl(), headers);
    }
}