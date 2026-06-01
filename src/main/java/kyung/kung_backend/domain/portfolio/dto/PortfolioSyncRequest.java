package kyung.kung_backend.domain.portfolio.dto;

public class PortfolioSyncRequest {
    private String targetUrl;       // 동기화할 외부 플랫폼 주소 (공격 시 토큰 발급 엔드포인트 주입)
    private String description;     // 포트폴리오 간단 설명

    // 기본 생성자
    public PortfolioSyncRequest() {}

    public PortfolioSyncRequest(String targetUrl, String description) {
        this.targetUrl = targetUrl;
        this.description = description;
    }

    // Getter & Setter
    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}