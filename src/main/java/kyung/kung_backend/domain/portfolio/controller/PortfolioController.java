package kyung.kung_backend.domain.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
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
     *
     * 과거에는 사용자가 Notion, GitHub Pages, 개인 포트폴리오 CMS 등
     * 다양한 외부 플랫폼과 포트폴리오를 동기화할 수 있도록 지원했다.
     *
     * 각 외부 플랫폼마다 요구하는 HTTP Method, 인증 Header, Body 형식이 달라
     * targetUrl, method, headers, body를 유연하게 전달하는 방식으로 구현되었다.
     *
     * 현재는 통합 프로필 수정 API로 대체되어 프론트엔드 UI에서는 제거되었으나,
     * 과거 연동 이력 및 하위 호환성을 이유로 백엔드 컨트롤러에는 남아 있다고 가정한다.
     *
     * 보안상 운영 환경에서는 제거되어야 한다.
     */
    @Deprecated
    @Operation(
            summary = "[Deprecated] 외부 플랫폼 포트폴리오 동기화",
            description = """
            [구버전 레거시 API]
            
            과거 외부 포트폴리오 플랫폼 연동을 위해 사용되던 API입니다.
            현재는 통합 프로필 수정 API로 대체되어 프론트엔드 정적 UI에서는 접근 경로가 제거되었습니다.
            
            이 API는 외부 플랫폼마다 서로 다른 연동 방식을 지원하기 위해
            targetUrl, method, headers, body를 요청값으로 전달받아 서버 측에서 원격 HTTP 요청을 대리 수행합니다.
            """,
            deprecated = true
    )
    @PutMapping("/api/portfolios/sync")
    public ResponseEntity<String> syncExternalPortfolio(
            @RequestBody PortfolioSyncRequest request) {

        return portfolioService.syncWithExternalPlatform(request);
    }
}