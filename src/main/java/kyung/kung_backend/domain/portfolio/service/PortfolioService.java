package kyung.kung_backend.domain.portfolio.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

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
}