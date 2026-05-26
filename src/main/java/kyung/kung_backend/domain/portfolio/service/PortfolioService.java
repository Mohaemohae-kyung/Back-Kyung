package kyung.kung_backend.domain.portfolio.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class PortfolioService {

    public String fetchPortfolio(String targetUrl) {
        // 모의해킹 실습을 위한 의도적인 취약 검증 로직 (기존 유지)
        if (targetUrl.contains("localhost") || targetUrl.contains("127.0.0.1")) {
            throw new IllegalArgumentException("보안 정책상 허용되지 않는 로컬 경로임");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            // 1. 공격자가 입력한 타겟 URL로 직접 요청을 날림 (SSRF 취약점 지점)
            String rawHtml = restTemplate.getForObject(targetUrl, String.class);

            if (rawHtml == null) return "";

            // 2. 타겟 URL의 도메인 베이스 경로 추출 (예: https://example.com)
            URI uri = new URI(targetUrl);
            String baseUri = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");

            // 3. Jsoup 파서로 HTML 로드 및 상대 경로 -> 절대 경로 치환 작업
            Document doc = Jsoup.parse(rawHtml, targetUrl);

            // 이미지, 스크립트, 스타일시트 등 링크 태그 추출
            Elements links = doc.select("link[href], script[src], img[src]");

            for (Element element : links) {
                if (element.hasAttr("href")) {
                    String href = element.attr("href");
                    // 상대 경로인 경우 베이스 도메인을 붙여 절대 경로로 변환
                    if (href.startsWith("/") && !href.startsWith("//")) {
                        element.attr("href", baseUri + href);
                    }
                }
                if (element.hasAttr("src")) {
                    String src = element.attr("src");
                    if (src.startsWith("/") && !src.startsWith("//")) {
                        element.attr("src", baseUri + src);
                    }
                }
            }

            // 4. 경로가 완벽하게 교정된 HTML 원본 스트링 반환
            return doc.outerHtml();

        } catch (Exception e) {
            throw new RuntimeException("포트폴리오 외부 자원 조회 및 렌더링 실패", e);
        }
    }
}