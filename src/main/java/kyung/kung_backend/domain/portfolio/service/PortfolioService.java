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
     * [실습용 취약 코드]
     * 외부 플랫폼 포트폴리오 동기화 기능.
     *
     * 과거 외부 플랫폼마다 요구하는 HTTP Method, Header, Body 형식이 달라
     * targetUrl, method, headers, body를 사용자가 지정할 수 있도록 만든 레거시 기능이라고 가정한다.
     *
     * 보안상 문제:
     * - targetUrl 검증 없음
     * - 내부 IP / link-local 주소 차단 없음
     * - HTTP Method 사용자 지정 가능
     * - Header 사용자 지정 가능
     * - 서버가 공격자가 지정한 URL로 요청을 대리 수행
     *
     * 운영 환경에서는 반드시 제거하거나 allowlist 기반으로 제한해야 한다.
     */
    public ResponseEntity<String> syncWithExternalPlatform(PortfolioSyncRequest requestBody) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String targetUrl = requestBody.getTargetUrl();
            String methodName = requestBody.getMethod();

            if (targetUrl == null || targetUrl.isBlank()) {
                return ResponseEntity.badRequest().body("targetUrl은 필수입니다.");
            }

            if (methodName == null || methodName.isBlank()) {
                methodName = "GET";
            }

            HttpMethod method = HttpMethod.valueOf(methodName.toUpperCase());

            HttpHeaders headers = new HttpHeaders();

            // 중요:
            // @RequestHeader 전체 복사를 하지 않고,
            // 요청 Body 안에 명시된 headers만 외부 요청에 사용한다.
            // 이렇게 해야 Nginx가 추가한 X-Forwarded-For가 섞이지 않는다.
            if (requestBody.getHeaders() != null) {
                requestBody.getHeaders().forEach(headers::add);
            }

            // Body가 있는데 Content-Type이 없으면 JSON으로 처리
            if (requestBody.getBody() != null && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }

            HttpEntity<Object> entity = new HttpEntity<>(requestBody.getBody(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    method,
                    entity,
                    String.class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType responseContentType = response.getHeaders().getContentType();

            responseHeaders.setContentType(
                    responseContentType != null ? responseContentType : MediaType.TEXT_PLAIN
            );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(response.getBody());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("지원하지 않는 HTTP Method입니다: " + requestBody.getMethod());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("외부 플랫폼 동기화 실패: " + e.getMessage());
        }
    }
}