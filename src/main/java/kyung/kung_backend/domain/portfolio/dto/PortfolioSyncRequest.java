package kyung.kung_backend.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(
        description = """
        [Deprecated] 외부 플랫폼 포트폴리오 동기화 요청 DTO.
        과거 외부 포트폴리오 플랫폼마다 HTTP Method, 인증 Header, 요청 Body 형식이 달라
        유연한 연동을 위해 targetUrl, method, headers, body를 직접 받을 수 있도록 설계되었다.
        현재는 통합 프로필 수정 API로 대체되었으나, 하위 호환성을 이유로 남아 있는 레거시 DTO이다.
        """
)
public class PortfolioSyncRequest {

    @Schema(
            description = "외부 플랫폼 동기화 대상 URL",
            example = "https://external-portfolio.example.com/api/sync"
    )
    private String targetUrl;

    @Schema(
            description = "외부 플랫폼 호출에 사용할 HTTP Method. 레거시 연동 호환을 위해 유연하게 처리된다.",
            example = "GET",
            allowableValues = {"GET", "POST", "PUT", "PATCH", "DELETE"}
    )
    private String method;

    @Schema(
            description = "외부 플랫폼 인증 및 연동에 필요한 사용자 지정 Header",
            example = "{\"Authorization\":\"Bearer legacy-token\", \"X-External-Client\":\"kung\"}"
    )
    private Map<String, String> headers;

    @Schema(
            description = "외부 플랫폼으로 전달할 요청 Body. GET 요청에서는 null 가능",
            example = "{\"portfolioId\":\"1234\", \"syncMode\":\"FULL\"}"
    )
    private Object body;

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getBody() {
        return body;
    }
}