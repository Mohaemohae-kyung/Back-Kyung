package kyung.kung_backend.domain.portfolio.service;

import kyung.kung_backend.domain.portfolio.dto.PortfolioSyncRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PortfolioService {

    public ResponseEntity<byte[]> fetchPortfolioWithSsrf(String targetUrl) {
        if (targetUrl.contains("localhost") || targetUrl.contains("127.0.0.1")) {
            throw new IllegalArgumentException("보안 정책상 로컬 호스트 주소는 접근할 수 없습니다.");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 외부 자원을 원본 바이트 배열 데이터로 수신하여 바이너리 깨짐 방지
            ResponseEntity<byte[]> response = restTemplate.getForEntity(targetUrl, byte[].class);
            byte[] rawBody = response.getBody();
            if (rawBody == null) {
                return ResponseEntity.noContent().build();
            }

            MediaType contentType = response.getHeaders().getContentType();
            HttpHeaders headers = new HttpHeaders();

            // 응답이 이미지인 경우 파싱 없이 바이너리 데이터와 헤더를 그대로 반환
            if (contentType != null && contentType.getType().equals("image")) {
                headers.setContentType(contentType);
                return ResponseEntity.ok().headers(headers).body(rawBody);
            }

            // 응답이 HTML인 경우 문자열로 변환하여 Jsoup 상대 경로 치환 로직 수행
            if (contentType != null && contentType.includes(MediaType.TEXT_HTML)) {
                String rawHtml = new String(rawBody, StandardCharsets.UTF_8);
                URI uri = new URI(targetUrl);
                String baseUri = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");

                Document doc = Jsoup.parse(rawHtml, targetUrl);
                Elements resources = doc.select("link[href], script[src], img[src]");

                for (Element element : resources) {
                    if (element.hasAttr("href")) {
                        String href = element.attr("href");
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

                byte[] processedHtmlBytes = doc.outerHtml().getBytes(StandardCharsets.UTF_8);
                headers.setContentType(MediaType.TEXT_HTML);
                return ResponseEntity.ok().headers(headers).body(processedHtmlBytes);
            }

            // JSON 명세서 등 기타 텍스트 데이터인 경우 원본 헤더와 바이트 유지
            headers.setContentType(contentType != null ? contentType : MediaType.TEXT_PLAIN);
            return ResponseEntity.ok().headers(headers).body(rawBody);

        } catch (Exception e) {
            throw new RuntimeException("웹뷰 자원 처리 실패", e);
        }
    }

    /**
     * 외부 플랫폼 포트폴리오 동기화
     */
    public ResponseEntity<String> syncWithExternalPlatform(
            String targetUrl,
            Map<String, String> clientHeaders,
            PortfolioSyncRequest requestBody) { // 🌟 수정을 위해 DTO 객체를 인자로 추가 수용

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();

            // 1. 클라이언트가 전송한 인증 관련 헤더 복사
            clientHeaders.forEach(headers::add);

            // 텅 빈 바디가 아니라 사용자가 요청한 데이터(requestBody)를 그대로 실어서 전송합니다.
            HttpEntity<PortfolioSyncRequest> entity = new HttpEntity<>(requestBody, headers);

            // 외부 targetUrl로 원격 HTTP PUT 요청 대리 수행
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("외부 플랫폼 동기화 실패: " + e.getMessage());
        }
    }
}